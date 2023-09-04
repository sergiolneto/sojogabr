# Importações necessárias
from flask import Flask, render_template, jsonify

app = Flask(__name__)

# Dados simulados para estatísticas de jogo e tabela de pontos
stats_data = {...}  # Substitua pelos seus dados reais

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/data')
def get_data():
    return jsonify(stats_data)

if __name__ == '__main__':
    app.run(debug=True)

