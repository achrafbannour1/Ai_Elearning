import sys
import re
from collections import Counter

def clean_text(text):
    # Nettoyage basique du texte
    text = re.sub(r'\s+', ' ', text.strip())
    text = re.sub(r'\([^)]*\)', '', text)  # supprime le contenu entre parenthèses
    return text

def tokenize_words(text):
    # Extraire les mots en minuscules
    words = re.findall(r'\b[a-zA-Zàâäéèêëîïôöùûüç]+\b', text.lower())
    return [w for w in words if len(w) > 3]

def summarize(text, n_sentences=3):
    if not text or len(text) < 50:
        return text  # Texte trop court → rien à résumer

    # Nettoyage
    text = clean_text(text)

    # Découper en phrases
    sentences = re.split(r'(?<=[.!?]) +', text)
    if len(sentences) <= n_sentences:
        return text  # Trop peu de phrases → retourne tout

    # Compter la fréquence des mots
    words = tokenize_words(text)
    freq = Counter(words)
    max_freq = max(freq.values()) if freq else 1
    for w in freq:
        freq[w] /= max_freq  # normalisation

    # Calculer le score de chaque phrase selon la somme des fréquences des mots
    sentence_scores = {}
    for sent in sentences:
        sent_clean = re.sub(r'[^a-zA-Zàâäéèêëîïôöùûüç ]', '', sent.lower())
        words_in_sent = sent_clean.split()
        if len(words_in_sent) < 4:
            continue
        score = sum(freq.get(w, 0) for w in words_in_sent)
        sentence_scores[sent] = score / len(words_in_sent)

    # Trier les phrases par score
    ranked_sentences = sorted(sentence_scores.items(), key=lambda x: x[1], reverse=True)
    top_sentences = [sent for sent, score in ranked_sentences[:n_sentences]]

    # Garder l’ordre original pour un résumé fluide
    summary = [s for s in sentences if s in top_sentences]
    return " ".join(summary)

if __name__ == "__main__":
    input_text = sys.stdin.read()
    print(summarize(input_text))

