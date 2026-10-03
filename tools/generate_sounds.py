import subprocess
import tempfile
from pathlib import Path

import numpy as np
from scipy import signal
from scipy.io import wavfile

RATE = 44100
OUT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/portalgunclassic/sounds"


def t_axis(duration):
    return np.arange(int(RATE * duration)) / RATE


def envelope(n, attack, release_power):
    env = np.exp(-release_power * np.linspace(0, 1, n))
    a = max(1, int(RATE * attack))
    env[:a] *= np.linspace(0, 1, a)
    return env


def sweep(duration, f0, f1, curve=1.0):
    t = t_axis(duration)
    k = (t / duration) ** curve
    freq = f0 + (f1 - f0) * k
    return np.sin(2 * np.pi * np.cumsum(freq) / RATE)


def band_noise(rng, duration, f0, f1, q_width=0.35):
    n = int(RATE * duration)
    white = rng.standard_normal(n)
    out = np.zeros(n)
    chunks = 24
    size = n // chunks + 1
    for i in range(chunks):
        center = f0 + (f1 - f0) * i / (chunks - 1)
        lo = max(40.0, center * (1 - q_width))
        hi = min(RATE / 2 - 100, center * (1 + q_width))
        sos = signal.butter(2, [lo, hi], btype="band", fs=RATE, output="sos")
        seg = signal.sosfilt(sos, white)
        out[i * size:(i + 1) * size] = seg[i * size:(i + 1) * size]
    return out / (np.max(np.abs(out)) + 1e-9)


def highpass(x, cutoff):
    sos = signal.butter(2, cutoff, btype="high", fs=RATE, output="sos")
    return signal.sosfilt(sos, x)


def lowpass(x, cutoff):
    sos = signal.butter(2, cutoff, btype="low", fs=RATE, output="sos")
    return signal.sosfilt(sos, x)


def finish(x, peak=0.8):
    x = np.nan_to_num(x)
    fade = int(RATE * 0.004)
    x[:fade] *= np.linspace(0, 1, fade)
    x[-fade:] *= np.linspace(1, 0, fade)
    return x / (np.max(np.abs(x)) + 1e-9) * peak


def fire(rng, base, duration):
    n = int(RATE * duration)
    tone = sweep(duration, base * 1.6, base * 0.35, 0.6) * envelope(n, 0.002, 5.0)
    fm = np.sin(2 * np.pi * np.cumsum(base * 0.5 + 400 * np.exp(-6 * t_axis(duration))) / RATE) * envelope(n, 0.002, 7.0)
    hiss = highpass(rng.standard_normal(n), 3500) * envelope(n, 0.001, 9.0)
    return finish(0.6 * tone + 0.3 * fm + 0.25 * hiss)


def opening(rng, base, duration):
    n = int(RATE * duration)
    swirl = band_noise(rng, duration, base * 0.4, base * 2.2) * envelope(n, 0.05, 2.2)
    rise_a = sweep(duration, base * 0.5, base * 1.5, 1.4)
    rise_b = sweep(duration, base * 0.503, base * 1.508, 1.4)
    shimmer = np.sin(2 * np.pi * 7 * t_axis(duration)) * 0.3 + 0.7
    tone = (rise_a + rise_b) * 0.5 * shimmer * envelope(n, 0.08, 2.8)
    return finish(0.55 * swirl + 0.55 * tone)


def enter(rng, base, duration):
    n = int(RATE * duration)
    whoosh = band_noise(rng, duration, base * 2.0, base * 0.4) * envelope(n, 0.01, 3.5)
    tone = sweep(duration, base * 1.4, base * 0.3, 0.8) * envelope(n, 0.005, 4.0)
    return finish(0.6 * whoosh + 0.5 * tone)


def exit_(rng, base, duration):
    n = int(RATE * duration)
    whoosh = band_noise(rng, duration, base * 0.4, base * 2.0) * envelope(n, 0.01, 3.0)
    tone = sweep(duration, base * 0.3, base * 1.5, 0.8) * envelope(n, 0.005, 3.5)
    pop = np.sin(2 * np.pi * base * 0.7 * t_axis(duration)) * np.exp(-40 * t_axis(duration))
    return finish(0.55 * whoosh + 0.5 * tone + 0.4 * pop)


def fizzle(rng, base, duration):
    n = int(RATE * duration)
    t = t_axis(duration)
    crackle = rng.standard_normal(n) * (rng.random(n) > 0.82)
    crackle = highpass(crackle, 1500) * envelope(n, 0.002, 4.0)
    buzz = np.sign(np.sin(2 * np.pi * 42 * t)) * (0.5 + 0.5 * np.sin(2 * np.pi * 9 * t))
    body = lowpass(band_noise(rng, duration, base, base * 0.3), 3000) * buzz * envelope(n, 0.003, 3.0)
    drop = sweep(duration, base * 0.9, base * 0.12, 0.7) * envelope(n, 0.003, 5.0)
    return finish(0.7 * crackle + 0.4 * body + 0.4 * drop)


def invalid(rng, base, duration):
    n = int(RATE * duration)
    t = t_axis(duration)
    saw_a = signal.sawtooth(2 * np.pi * base * t)
    saw_b = signal.sawtooth(2 * np.pi * base * 1.06 * t)
    tremolo = 0.55 + 0.45 * np.sign(np.sin(2 * np.pi * 22 * t))
    buzz = lowpass((saw_a + saw_b) * 0.5, 1400) * tremolo * envelope(n, 0.004, 2.5)
    thud = np.sin(2 * np.pi * base * 0.5 * t) * np.exp(-18 * t)
    return finish(0.7 * buzz + 0.5 * thud)


def active(rng, base, duration):
    n = int(RATE * duration)
    half = n // 2
    first = np.sin(2 * np.pi * base * t_axis(duration)[:half]) * envelope(half, 0.003, 4.0)
    second = np.sin(2 * np.pi * base * 1.5 * t_axis(duration)[:n - half]) * envelope(n - half, 0.003, 4.5)
    return finish(np.concatenate([first, second]), 0.6)


SOUNDS = {
    "portal_enter_01": (enter, 700, 0.30, 1),
    "portal_enter_02": (enter, 780, 0.28, 2),
    "portal_enter_03": (enter, 640, 0.34, 3),
    "portal_exit_01": (exit_, 700, 0.32, 4),
    "portal_exit_02": (exit_, 620, 0.36, 5),
    "portal_fizzle_01": (fizzle, 700, 0.55, 6),
    "portal_fizzle_02": (fizzle, 560, 0.50, 7),
    "portal_invalid_01": (invalid, 140, 0.28, 8),
    "portal_invalid_02": (invalid, 120, 0.30, 9),
    "portal_invalid_03": (invalid, 165, 0.24, 10),
    "portal_invalid_04": (invalid, 105, 0.32, 11),
    "portal_open_blue_01": (opening, 900, 0.65, 12),
    "portal_open_red_01": (opening, 520, 0.70, 13),
    "portal_open_red_02": (opening, 470, 0.62, 14),
    "wpn_portal_gun_fire_b01": (fire, 1500, 0.32, 15),
    "wpn_portal_gun_fire_b02": (fire, 1650, 0.30, 16),
    "wpn_portal_gun_fire_b03": (fire, 1400, 0.34, 17),
    "wpn_portal_gun_fire_r01": (fire, 950, 0.36, 18),
    "wpn_portal_gun_fire_r02": (fire, 880, 0.38, 19),
    "wpn_portal_gun_fire_r03": (fire, 1020, 0.34, 20),
    "wpn_portalgun_active_01": (active, 620, 0.20, 21),
}


def encode(name, samples):
    with tempfile.TemporaryDirectory() as tmp:
        wav = Path(tmp) / f"{name}.wav"
        wavfile.write(wav, RATE, (samples * 32767).astype(np.int16))
        subprocess.run(
            ["ffmpeg", "-y", "-loglevel", "error", "-i", str(wav), "-ac", "1", "-c:a", "libvorbis", "-q:a", "4", str(OUT / f"{name}.ogg")],
            check=True,
        )


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    for name, (builder, base, duration, seed) in SOUNDS.items():
        encode(name, builder(np.random.default_rng(seed), base, duration))


if __name__ == "__main__":
    main()
