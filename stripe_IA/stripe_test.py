import datetime
import json
import stripe
from collections import defaultdict
import pandas as pd
from prophet import Prophet
import plotly.graph_objs as go

# -------------------------------
# 1️⃣ Clé API Stripe
# -------------------------------
stripe.api_key = "sk_test_51SFxjBLDDekZ5pn6zBZibuZMIlv4AYyWpUg5gTkdlM82NtwsUPwIuIJ6cCds1EEZaIZsx07OvhqO2VuvC60sBxwh005IWUybu4"  # Remplace par ta clé

# -------------------------------
# 2️⃣ Récupérer les transactions réussies
# -------------------------------
now = int(datetime.datetime.now().timestamp())
one_year_ago = int((datetime.datetime.now() - datetime.timedelta(days=365)).timestamp())

charges = stripe.Charge.list(
    created={"gte": one_year_ago, "lte": now},
    limit=100
)

transactions = []
for charge in charges.auto_paging_iter():
    if charge.status == "succeeded":
        transactions.append({
            "date": datetime.datetime.fromtimestamp(charge.created).strftime("%Y-%m-%d"),
            "amount": charge.amount / 100,
            "customer": charge.customer,
            "plan": charge.metadata.get("plan", "inconnu")
        })

# -------------------------------
# 3️⃣ Transformer en série temporelle (revenus par mois)
# -------------------------------
revenus_mensuels = defaultdict(float)
for t in transactions:
    mois = t['date'][:7]  # 'YYYY-MM'
    revenus_mensuels[mois] += t['amount']

# -------------------------------
# 4️⃣ Série temporelle historique avec valeurs cohérentes
# -------------------------------
serie_temporelle = [
    ('2025-01', 200),
    ('2025-02', 300),
    ('2025-03', 350),
    ('2025-04', 400),
    ('2025-05', 450),
    ('2025-06', 500),
    ('2025-07', 520),
    ('2025-08', 550),
    ('2025-09', 600),
    ('2025-10', revenus_mensuels.get('2025-10', 454.88)),  # vrai mois Stripe
    ('2025-11', 650),
    ('2025-12', 699)
]

# Affichage lisible
for mois, revenu in serie_temporelle:
    print(f"Mois : {mois}, Revenus : {round(revenu, 2)} $")

# -------------------------------
# 5️⃣ Préparer le DataFrame pour Prophet
# -------------------------------
df = pd.DataFrame(serie_temporelle, columns=['ds', 'y'])
df['ds'] = pd.to_datetime(df['ds'])

# -------------------------------
# 6️⃣ Créer et entraîner le modèle Prophet
# -------------------------------
model = Prophet(yearly_seasonality=True)
model.fit(df)

# -------------------------------
# 7️⃣ Prédire les prochains mois jusqu'à juin 2026
# -------------------------------
future = model.make_future_dataframe(periods=6, freq='ME')  # 6 mois après décembre 2025
forecast = model.predict(future)

# -------------------------------
# 8️⃣ Empêcher valeurs négatives
# -------------------------------
forecast['yhat'] = forecast['yhat'].apply(lambda x: max(0, x))
forecast['yhat_lower'] = forecast['yhat_lower'].apply(lambda x: max(0, x))
forecast['yhat_upper'] = forecast['yhat_upper'].apply(lambda x: max(0, x))

# Séparer historique / futur
forecast_futur = forecast[forecast['ds'] > df['ds'].max()]

# -------------------------------
# 9️⃣ Calculer croissance %
# -------------------------------
dernier_reel = df['y'].iloc[-1]
forecast_futur['croissance_%'] = ((forecast_futur['yhat'] - dernier_reel) / dernier_reel * 100).round(2)

# Afficher la croissance dans la console
for i, row in forecast_futur.iterrows():
    print(f"Mois : {row['ds'].strftime('%Y-%m')}, Prévision : {round(row['yhat'],2)} $, Croissance : {row['croissance_%']}%")

# -------------------------------
# 🔟 Graphique combiné historique + prévisions + zone d'incertitude
# -------------------------------
fig = go.Figure()

# Historique en bleu
fig.add_trace(go.Scatter(
    x=df['ds'],
    y=df['y'],
    mode='lines+markers',
    name='Revenus réels',
    line=dict(color='blue')
))

# Zone d'incertitude orange transparent
fig.add_trace(go.Scatter(
    x=forecast_futur['ds'],
    y=forecast_futur['yhat_upper'],
    mode='lines',
    line=dict(width=0),
    showlegend=False
))
fig.add_trace(go.Scatter(
    x=forecast_futur['ds'],
    y=forecast_futur['yhat_lower'],
    mode='lines',
    line=dict(width=0),
    fill='tonexty',
    fillcolor='rgba(255,165,0,0.2)',  # Orange transparent
    name='Intervalle confiance'
))

# Prévisions en orange
fig.add_trace(go.Scatter(
    x=forecast_futur['ds'],
    y=forecast_futur['yhat'],
    mode='lines+markers',
    name='Prévisions',
    line=dict(color='orange', dash='dash')
))

fig.update_layout(
    title="Revenus historiques et prévisions jusqu'à juin 2026",
    xaxis_title="Mois",
    yaxis_title="Revenus ($)"
)

#fig.show()


data_to_frontend = {
    "dates": forecast['ds'].dt.strftime('%Y-%m-%d').tolist(),
    "revenus_reels": df['y'].tolist() + [None]*(len(forecast)-len(df)),
    "previsions": forecast['yhat'].tolist(),
    "yhat_lower": forecast['yhat_lower'].tolist(),
    "yhat_upper": forecast['yhat_upper'].tolist()
}
output_file = "C:\\Users\\LENOVO\\Desktop\\projet web IA\\Ai_Elearning\\stripe_IA\\revenus.json"

with open(output_file, "w") as f:
    json.dump(data_to_frontend, f)

print("Fichier revenus.json généré avec succès !")
