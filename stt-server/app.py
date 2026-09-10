from flask import Flask, request, jsonify
from stt_model import speech_to_text
import tempfile
import os

app = Flask(__name__)


@app.route("/")
def home():
    return "iTantra STT Server is running!"


@app.route("/stt", methods=["POST"])
def stt():
    audio_file = request.files.get("audio")
    language = request.form.get("language", "hi")

    if audio_file is None:
        return jsonify({"error": "No audio file provided"}), 400

    temp_path = None

    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix=".ogg") as temp:
            audio_file.save(temp.name)
            temp_path = temp.name

        text = speech_to_text(temp_path, language)

        return jsonify({
            "text": text,
            "language": language
        })

    except Exception as e:
        return jsonify({
            "error": str(e)
        }), 500

    finally:
        if temp_path and os.path.exists(temp_path):
            os.remove(temp_path)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)