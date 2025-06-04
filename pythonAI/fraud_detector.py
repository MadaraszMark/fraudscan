import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report
import joblib
from collections import deque

data = pd.read_csv("generated_training_data.txt", sep="\t")

data["timestamp"] = pd.to_datetime(data["timestamp"])
data["hour"] = data["timestamp"].dt.hour
data["minute"] = data["timestamp"].dt.minute

data = data.sort_values(by="timestamp")

recent_counts = []
recent_window = deque()

for i, row in data.iterrows():
    current_time = row["timestamp"]
    acc = row["from_account"]

    while recent_window and (current_time - recent_window[0][0]).total_seconds() > 120:
        recent_window.popleft()

    count = sum(1 for t, a in recent_window if a == acc)
    recent_counts.append(count)

    recent_window.append((current_time, acc))

data["recent_count_from_account"] = recent_counts

features = data[["from_account", "to_account", "amount", "hour", "minute", "recent_count_from_account"]].copy()
features["from_account"] = features["from_account"].astype(str).astype(int)
features["to_account"] = features["to_account"].astype(str).astype(int)

labels = data["is_fraud"]

X_train, X_test, y_train, y_test = train_test_split(features, labels, test_size=0.2, random_state=42)

model = RandomForestClassifier(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

y_pred = model.predict(X_test)
print(classification_report(y_test, y_pred))

joblib.dump(model, "fraud_model.pkl")
print("✅ Modell fájlba mentve: fraud_model.pkl")

#uvicorn app:app --reload
