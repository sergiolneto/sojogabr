document.addEventListener('DOMContentLoaded', () => {
    const loginBtn = document.getElementById('login-btn');
    const loginPopup = document.getElementById('login-popup');
    const closeBtn = loginPopup.querySelector('.close-btn');
    const loginForm = document.getElementById('login-form');

    // 1. Verifica o estado de login assim que a página carrega
    checkLoginState();

    // 2. Configura os eventos dos botões
    if (loginBtn) {
        loginBtn.addEventListener('click', (e) => {
            e.preventDefault();
            loginPopup.style.display = 'flex';
        });
    }

    closeBtn.addEventListener('click', () => {
        loginPopup.style.display = 'none';
    });

    window.addEventListener('click', (e) => {
        if (e.target === loginPopup) {
            loginPopup.style.display = 'none';
        }
    });

    loginForm.addEventListener('submit', handleLogin);
});

/**
 * Verifica se há um token no localStorage e atualiza a UI de acordo.
 */
function checkLoginState() {
    const token = localStorage.getItem('token');
    if (token) {
        try {
            // Decodifica o payload do JWT para obter os dados do usuário
            const payload = JSON.parse(atob(token.split('.')[1]));
            const username = payload.sub; // 'sub' (subject) geralmente contém o username

            updateNavForLoggedInUser(username);
        } catch (error) {
            console.error('Erro ao decodificar o token:', error);
            // Se o token for inválido, limpa e volta ao estado de logout
            handleLogout();
        }
    }
}

/**
 * Atualiza a barra de navegação para um usuário logado.
 * @param {string} username O nome do usuário para exibir.
 */
function updateNavForLoggedInUser(username) {
    const loginNavItem = document.getElementById('login-nav-item');
    if (loginNavItem) {
        loginNavItem.innerHTML = `
            <a href="#" id="username-display">Olá, ${username}</a>
            <a href="#" id="logout-btn" style="margin-left: 15px;">Logout</a>
        `;

        // Adiciona o evento de clique para o novo botão de logout
        document.getElementById('logout-btn').addEventListener('click', (e) => {
            e.preventDefault();
            handleLogout();
        });
    }
}

/**
 * Realiza o logout do usuário, limpando o token e recarregando a página.
 */
function handleLogout() {
    localStorage.removeItem('token');
    window.location.reload();
}

/**
 * Envia as credenciais de login para a API e trata a resposta.
 * @param {Event} event O evento de submissão do formulário.
 */
async function handleLogin(event) {
    event.preventDefault();

    const usernameInput = document.getElementById('popup-username');
    const passwordInput = document.getElementById('popup-password');

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

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            // Não precisa de alert, o recarregamento vai atualizar a UI
            window.location.reload();
        } else {
            let errorMessage = 'Erro ao fazer login. Verifique suas credenciais.';
            try {
                const errorData = await response.json();
                if (errorData && errorData.message) {
                    errorMessage = errorData.message;
                }
            } catch (e) {
                console.error('Não foi possível parsear a resposta de erro como JSON.', e);
            }
            Swal.fire({
                title: 'Erro de Login',
                text: errorMessage,
                icon: 'error',
                confirmButtonText: 'Tentar Novamente'
            });
        }
    } catch (error) {
        console.error('Erro de rede ao tentar fazer login:', error);
        Swal.fire({
            title: 'Erro de Conexão',
            text: 'Não foi possível conectar ao servidor. Tente novamente mais tarde.',
            icon: 'error',
            confirmButtonText: 'Ok'
        });
    }
}
