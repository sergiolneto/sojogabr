document.addEventListener('DOMContentLoaded', () => {
    const loginBtn = document.getElementById('login-btn');
    const loginPopup = document.getElementById('login-popup');
    const closeBtn = loginPopup.querySelector('.close-btn');
    const loginForm = document.getElementById('login-form');

    // Abre o popup de login
    loginBtn.addEventListener('click', (e) => {
        e.preventDefault();
        loginPopup.style.display = 'flex';
    });

    // Fecha o popup de login
    closeBtn.addEventListener('click', () => {
        loginPopup.style.display = 'none';
    });

    // Fecha o popup se clicar fora do conteúdo
    window.addEventListener('click', (e) => {
        if (e.target === loginPopup) {
            loginPopup.style.display = 'none';
        }
    });

    // Manipula o envio do formulário de login
    loginForm.addEventListener('submit', handleLogin);
});

/**
 * Envia as credenciais de login para a API e trata a resposta.
 * @param {Event} event O evento de submissão do formulário.
 */
async function handleLogin(event) {
    event.preventDefault();

    const usernameInput = document.getElementById('popup-username');
    const passwordInput = document.getElementById('popup-password');
    const errorMessageDiv = document.getElementById('login-error-message');

    const loginData = {
        username: usernameInput.value,
        password: passwordInput.value
    };

    try {
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });

        // Limpa mensagens de erro anteriores
        errorMessageDiv.style.display = 'none';
        errorMessageDiv.textContent = '';

        if (response.ok) {
            // Se o login for bem-sucedido (status 2xx)
            const data = await response.json();
            localStorage.setItem('token', data.token);
            alert('Login bem-sucedido!');
            window.location.reload(); // Recarrega a página para refletir o estado de login
        } else {
            // Se o servidor retornar um erro (4xx, 5xx)
            let errorMessage = 'Erro ao fazer login. Verifique suas credenciais.';
            try {
                // Tenta extrair uma mensagem de erro mais específica do corpo da resposta
                const errorData = await response.json();
                if (errorData && errorData.message) {
                    errorMessage = errorData.message;
                }
            } catch (e) {
                // O corpo da resposta não é um JSON ou está vazio, usa a mensagem padrão.
                console.error('Não foi possível parsear a resposta de erro como JSON.', e);
            }
            errorMessageDiv.textContent = errorMessage;
            errorMessageDiv.style.display = 'block';
        }
    } catch (error) {
        // Captura erros de rede (ex: servidor offline)
        console.error('Erro de rede ao tentar fazer login:', error);
        errorMessageDiv.textContent = 'Não foi possível conectar ao servidor. Tente novamente mais tarde.';
        errorMessageDiv.style.display = 'block';
    }
}
