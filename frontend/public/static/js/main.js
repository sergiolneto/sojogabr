document.addEventListener('DOMContentLoaded', () => {
    const loginPopup = document.getElementById('login-popup');
    const closeBtn = document.querySelector('.popup-content .close-btn');
    const loginForm = document.getElementById('login-form');
    const loginErrorMessage = document.getElementById('login-error-message');
    const menuUl = document.querySelector('.menu ul');

    // Função para atualizar a UI para o estado "logado"
    const updateUIForLogin = (username) => {
        const loginLi = document.getElementById('login-nav-item');
        if (loginLi) {
            // Cria os novos itens de menu
            const welcomeLi = document.createElement('li');
            welcomeLi.innerHTML = `<span>Olá, ${username}</span>`;

            const logoffLi = document.createElement('li');
            logoffLi.innerHTML = `<a href="#" id="logoff-btn">Logoff</a>`;

            // Substitui o item de login pelos novos
            const parentUl = loginLi.parentNode;
            parentUl.replaceChild(logoffLi, loginLi);
            parentUl.insertBefore(welcomeLi, logoffLi);

            // Adiciona o listener de evento para o novo botão de logoff
            document.getElementById('logoff-btn').addEventListener('click', handleLogoff);
        }
    };

    // Verifica o status de login ao carregar a página
    const checkLoginStatus = async () => {
        try {
            // Endpoint que retorna os dados do usuário se a sessão for válida, ou 401 caso contrário
            const response = await fetch('/api/me');
            if (response.ok) {
                const user = await response.json();
                updateUIForLogin(user.username);
            } else {
                // Se não estiver logado, garante que o botão de login tenha o listener
                attachLoginButtonListener();
            }
        } catch (error) {
            console.error('Erro ao verificar status de login:', error);
            attachLoginButtonListener();
        }
    };

    // Lida com a submissão do formulário de login
    const handleLogin = async (event) => {
        event.preventDefault();
        const username = document.getElementById('popup-username').value;
        const password = document.getElementById('popup-password').value;

        try {
            const response = await fetch('/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                const user = await response.json();
                loginPopup.style.display = 'none';
                loginForm.reset();
                loginErrorMessage.style.display = 'none';
                updateUIForLogin(user.username);
            } else {
                loginErrorMessage.textContent = 'Usuário ou senha inválidos.';
                loginErrorMessage.style.display = 'block';
            }
        } catch (error) {
            console.error('Erro ao fazer login:', error);
            loginErrorMessage.textContent = 'Erro de conexão com o servidor.';
            loginErrorMessage.style.display = 'block';
        }
    };

    // Lida com o clique no botão de logoff
    const handleLogoff = async (event) => {
        event.preventDefault();
        try {
            const response = await fetch('/api/logout', { method: 'POST' });
            if (response.ok) {
                // Recarrega a página para garantir que todos os estados sejam limpos
                window.location.href = '/';
            } else {
                alert('Erro ao fazer logoff.');
            }
        } catch (error) {
            console.error('Erro ao fazer logoff:', error);
            alert('Erro de conexão ao tentar fazer logoff.');
        }
    };

    // Função para adicionar o listener ao botão de login
    const attachLoginButtonListener = () => {
        const loginBtn = document.getElementById('login-btn');
        if (loginBtn) {
            loginBtn.addEventListener('click', (e) => {
                e.preventDefault();
                loginPopup.style.display = 'flex';
            });
        }
    };

    // --- Listeners Iniciais ---

    // Esconde o popup de login
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            loginPopup.style.display = 'none';
        });
    }

    // Esconde o popup ao clicar fora dele
    window.addEventListener('click', (event) => {
        if (event.target === loginPopup) {
            loginPopup.style.display = 'none';
        }
    });

    // Adiciona o handler para o formulário de login
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Verifica o status de login ao carregar a página
    checkLoginStatus();
});