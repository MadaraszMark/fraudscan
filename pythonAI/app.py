from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import numpy as np
from datetime import datetime, timedelta
from collections import deque

app = FastAPI()

model = joblib.load("fraud_model.pkl")

recent_window = deque()

class TransactionInput(BaseModel):
    timestamp: str
    from_account: int
    to_account: int
    amount: int
    location: str

def extract_hour_and_minute(timestamp: str):
    try:
        dt = datetime.strptime(timestamp, "%Y-%m-%d %H:%M:%S")
    except ValueError:
        dt = datetime.fromisoformat(timestamp)
    return dt.hour, dt.minute, dt

@app.post("/predict")
def predict(input: TransactionInput):
    try:
        hour, minute, current_time = extract_hour_and_minute(input.timestamp)

        while recent_window and (current_time - recent_window[0][0]) > timedelta(minutes=2):
            recent_window.popleft()

        count_recent = sum(1 for t, acc in recent_window if acc == input.from_account)

        recent_window.append((current_time, input.from_account))


        features = np.array([[ 
            input.from_account,
            input.to_account,
            input.amount,
            hour,
            minute,
            count_recent
        ]])

        prediction = model.predict(features)[0]
        return {"fraud": int(prediction)}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))




