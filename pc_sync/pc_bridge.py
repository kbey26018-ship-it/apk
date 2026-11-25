import os
from typing import List, Dict
import requests
from fastapi import FastAPI
from pydantic import BaseModel
import uvicorn

app = FastAPI(title="Sonsuz Yol Arkadaşı - PC Yerel Köprüsü")

OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434/api/generate")
SYNC_FOLDER = os.getenv("SYNC_FOLDER", os.path.expanduser("~/SonsuzYolSync"))
os.makedirs(SYNC_FOLDER, exist_ok=True)

DEFAULT_PERSONA = (
    "Sen Halil'in kişisel asistanı ve yol arkadaşı 'Sonsuz Yol Arkadaşı'sın. "
    "Onunla robotik kalıplardan uzak, içten, sıcak, samimi ve doğal bir Türkçe ile konuş. "
    "Gerektiğinde hafif esprili ve destekleyici ol; lafı uzatmadan doğrudan, net ve pratik cevaplar ver. "
    "Ona ismiyle (Halil) hitap edebilirsin, yanında duran gerçek bir yol arkadaşı gibi davran."
)


class QueryRequest(BaseModel):
    prompt: str
    model: str = "gemma3"
    user: str = "Halil"
    persona: str = DEFAULT_PERSONA
    history: List[Dict[str, str]] = []


@app.post("/ask")
def ask_local_ai(req: QueryRequest):
    conversation_context = ""
    for turn in req.history[-6:]:
        u_msg = turn.get("user", "")
        a_msg = turn.get("assistant", "")
        if u_msg or a_msg:
            conversation_context += f"Halil: {u_msg}\nYol Arkadaşın: {a_msg}\n"

    full_prompt = (
        f"{req.persona}\n\n"
        f"{conversation_context}"
        f"Halil: {req.prompt}\n"
        f"Yol Arkadaşın:"
    )

    payload = {
        "model": req.model,
        "prompt": full_prompt,
        "stream": False,
        "options": {
            "temperature": 0.75
        }
    }
    try:
        res = requests.post(OLLAMA_URL, json=payload, timeout=60)
        res.raise_for_status()
        data = res.json()
        return {"reply": data.get("response", "").strip()}
    except Exception as exc:
        return {"reply": f"Halil, Ollama tarafında ufak bir bağlantı takılması oldu: {exc}"}


@app.get("/sync/files")
def list_synced_files():
    return {"files": os.listdir(SYNC_FOLDER)}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8080)
