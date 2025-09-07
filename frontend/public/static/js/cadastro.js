document.addEventListener('DOMContentLoaded', () => {
    // Corrigido o ID para corresponder ao do HTML ('cadastroForm')
    const registerForm = document.getElementById('cadastroForm');

    // Adicionado um listener de evento seguro que só executa se o formulário existir
    registerForm?.addEventListener('submit', async (event) => {
        event.preventDefault(); // Impede o envio tradicional do formulário

        const formData = new FormData(registerForm);

        // A forma correta de obter todos os valores de checkboxes com o mesmo nome
        const esportes = formData.getAll('esportes');

        // Cria o objeto de dados e garante que a propriedade 'esportes' seja um array
        const data = Object.fromEntries(formData.entries());
        data.esportes = esportes;

        try {
            // O endpoint correto para criação de usuário é /api/users
            const response = await fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                // Se o cadastro for bem-sucedido (status 201)
                Swal.fire({
                    title: 'Sucesso!',
                    text: 'Cadastro realizado com sucesso! Você será redirecionado para a página inicial.',
                    icon: 'success',
                    timer: 3000,
                    showConfirmButton: false
                }).then(() => {
                    window.location.href = '/'; // Redireciona para a página inicial
                });

            } else {
                // Se o servidor retornar um erro (400, 409, etc.)
                const errorData = await response.json();
                let errorMessage = '';

                if (response.status === 409) { // HTTP 409 Conflict - Usuário já existe
                    errorMessage = errorData.error;
                } else if (response.status === 400) { // Bad Request - Erros de validação
                    // Formata a lista de erros de validação
                    errorMessage = Object.values(errorData).join('\n');
                }

                Swal.fire({
                    title: 'Erro no Cadastro',
                    text: errorMessage,
                    icon: 'error'
                });
            }

        } catch (error) {
            // Captura erros de rede (ex: servidor offline)
            console.error('Erro de rede ao tentar cadastrar:', error);
            Swal.fire({
                title: 'Erro de Conexão',
                text: 'Não foi possível conectar ao servidor. Tente novamente mais tarde.' + error.message,
                icon: 'error'
            });
        }
    }); // Fim do addEventListener de submit

    // --- Lógica para exibir/ocultar o campo "Nome do Time" ---
    const sportCheckboxes = document.querySelectorAll('input[name="esportes"]');
    const teamNameGroup = document.getElementById('team-name-group');
    const teamNameInput = document.getElementById('time');

    // Garante que os elementos existem antes de adicionar os listeners
    if (teamNameGroup && teamNameInput && sportCheckboxes.length > 0) {
        const toggleTeamNameField = () => {
            // Verifica se algum esporte que não seja 'Tenis' está marcado
            const showTeamName = Array.from(sportCheckboxes).some(
                checkbox => checkbox.checked && checkbox.value !== 'Tenis'
            );

            if (showTeamName) {
                teamNameGroup.style.display = 'block';
            } else {
                teamNameGroup.style.display = 'none';
                teamNameInput.value = ''; // Limpa o campo se ele for ocultado
            }
        }

        sportCheckboxes.forEach(checkbox => checkbox.addEventListener('change', toggleTeamNameField));
    }
}); // Fim do DOMContentLoaded