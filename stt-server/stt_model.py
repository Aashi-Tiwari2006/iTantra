import torch
import torchaudio
import whisper
from transformers import AutoModel


# =========================
# Load English Whisper model
# =========================
print("Loading English Whisper model...")

whisper_model = whisper.load_model("base.en")

print("English Whisper model loaded.")


# =========================
# Load AI4Bharat Indic Conformer
# =========================
print("Loading AI4Bharat Indic Conformer model...")

model = AutoModel.from_pretrained(
    "ai4bharat/indic-conformer-600m-multilingual",
    trust_remote_code=True
)

print("AI4Bharat model loaded.")


# =========================
# Speech-to-Text function
# =========================
def speech_to_text(audio_file, language="hi"):

    # English → Whisper
    if language == "en":
        result = whisper_model.transcribe(
            audio_file,
            language="en"
        )

        return result["text"].strip()

    # Indian languages → AI4Bharat
    import soundfile as sf
    wav, sr = sf.read(audio_file)

    wav = torch.tensor(wav, dtype=torch.float32)

    if wav.ndim == 1:
        wav = wav.unsqueeze(0)
    else:
        wav = wav.transpose(0, 1)    # Convert stereo audio to mono
    if wav.shape[0] > 1:
        wav = wav.mean(dim=0, keepdim=True)

    # Convert sample rate to 16 kHz
    if sr != 16000:
        resampler = torchaudio.transforms.Resample(sr, 16000)
        wav = resampler(wav)

    # Run the model
    with torch.no_grad():
        text = model(wav, language, "ctc")

    return text.strip()