document.addEventListener('DOMContentLoaded', function() {
    // Fazer solicitação AJAX para obter estatísticas de jogo e tabela de pontos
    fetch('/api/data')
        .then(response => response.json())
        .then(data => {
            // Atualizar o conteúdo da página com os dados recebidos
            // Exemplo: document.getElementById('stats').innerHTML = data.stats;
        })
        .catch(error => {
            console.error('Erro ao buscar dados: ', error);
        });
});
