// --- Lógica para abrir e fechar o popup de login ---
const loginBtn = document.getElementById('login-btn');
const loginPopup = document.getElementById('login-popup');
const closeBtn = loginPopup.querySelector('.close-btn');

// Abre o popup ao clicar em "Login"
loginBtn.addEventListener('click', (e) => {
    e.preventDefault();
    loginPopup.style.display = 'flex';
});

// Fecha o popup no botão 'x'
closeBtn.addEventListener('click', () => {
    loginPopup.style.display = 'none';
});

// Fecha o popup se clicar fora dele
window.addEventListener('click', (e) => {
    if (e.target === loginPopup) {
        loginPopup.style.display = 'none';
    }
});

// --- Lógica para submissão do formulário de login ---
document.getElementById('login-form').addEventListener('submit', async function(event) {
    event.preventDefault();
    const errorMessageDiv = document.getElementById('login-error-message');
    errorMessageDiv.style.display = 'none'; // Esconde mensagens de erro antigas

    const loginData = {
        username: this.elements['username'].value,
        password: this.elements['password'].value
    };

    try {
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(loginData)
        });

        const result = await response.json();

        if (response.ok) { // Status 200-299
            // Sucesso! Atualiza a UI
            const loginNavItem = document.getElementById('login-nav-item');
            loginNavItem.innerHTML = `<span class="welcome-user">Bem-vindo, ${result.username}!</span>`;
            loginPopup.style.display = 'none'; // Fecha o popup
        } else {
            // Erro (ex: 401 Unauthorized)
            errorMessageDiv.textContent = result.message || 'Ocorreu um erro.';
            errorMessageDiv.style.display = 'block';
        }
    } catch (error) {
        errorMessageDiv.textContent = 'Erro de conexão com o servidor.';
        errorMessageDiv.style.display = 'block';
        console.error('Erro no login:', error);
    }
});

// --- Lógica para carregar e exibir os eventos ---
document.addEventListener('DOMContentLoaded', () => {
    fetchAndRenderEvents();
});

async function fetchAndRenderEvents() {
    const eventsGrid = document.getElementById('events-grid');
    // Limpa a área para o caso de recarregamento
    eventsGrid.innerHTML = '<p>Carregando eventos...</p>';

    try {
        // **PASSO FUTURO**: Substituir o mock por uma chamada fetch real
        // const response = await fetch('/api/events');
        // const events = await response.json();

        // **MOCK DATA**: Usando dados de exemplo para demonstração.
        // As imagens são links reais de posts do Instagram @sojogabr
        const events = [
            {
                id: '1',
                title: 'Futebol Society Masculino',
                sport: 'Futebol',
                location: 'Playball Pompéia',
                eventDate: '2025-09-15T20:00:00',
                imageUrl: 'https://scontent.cdninstagram.com/v/t51.2885-15/450419561_1173117150500985_3033575934253733398_n.jpg?_nc_cat=103&ccb=1-7&_nc_sid=18de74&_nc_ohc=d_u-Y6gqfFkQ7kNvgFv2YxG&_nc_ht=scontent.cdninstagram.com&edm=ANo9K5cEAAAA&oh=00_AYBq9y_l5hXGz_675i-o9B8yX2w_yW3_p-8Jb90c2-5_9w&oe=6683412B',
                eventType: 'JOGO'
            },
            {
                id: '2',
                title: 'Campeonato de Vôlei Misto',
                sport: 'Vôlei',
                location: 'Praia de Copacabana',
                eventDate: '2025-10-05T09:00:00',
                imageUrl: 'https://scontent.cdninstagram.com/v/t51.2885-15/449373279_494541819890471_362246968876933033_n.jpg?_nc_cat=109&ccb=1-7&_nc_sid=18de74&_nc_ohc=y9qR0_g_3dEQ7kNvgE9_3-m&_nc_ht=scontent.cdninstagram.com&edm=ANo9K5cEAAAA&oh=00_AYB3g822-1o97jL0yW5-12w0s_Y8K_g_j2y-y8z-96M5-Q&oe=668352F7',
                eventType: 'CAMPEONATO'
            },
            {
                id: '3',
                title: 'Basquete 3x3',
                sport: 'Basquete',
                location: 'Parque Villa-Lobos',
                eventDate: '2025-09-20T15:00:00',
                imageUrl: 'https://scontent.cdninstagram.com/v/t51.2885-15/448828983_1194522918386377_301019183983271846_n.jpg?_nc_cat=106&ccb=1-7&_nc_sid=18de74&_nc_ohc=t-0_YgYg_0wQ7kNvgGf6e3E&_nc_ht=scontent.cdninstagram.com&edm=ANo9K5cEAAAA&oh=00_AYD7Xy6_p9-8_yW3_zW3_xW3_yW3_zW3_xW3_yW3&oe=66834A1F',
                eventType: 'JOGO'
            }
        ];

        if (events.length === 0) {
            eventsGrid.innerHTML = '<p>Nenhum evento agendado no momento.</p>';
            return;
        }

        eventsGrid.innerHTML = ''; // Limpa a mensagem de "carregando"
        events.forEach(event => {
            const eventDate = new Date(event.eventDate);
            const formattedDate = `${eventDate.toLocaleDateString('pt-BR')} às ${eventDate.toLocaleTimeString('pt-BR', {hour: '2-digit', minute:'2-digit'})}`;

            const card = `
                        <div class="event-card">
                            <img src="${event.imageUrl}" alt="${event.title}" class="event-card-img">
                            <div class="event-card-body">
                                <span class="event-card-tag">${event.sport}</span>
                                <h3 class="event-card-title">${event.title}</h3>
                                <p class="event-card-info">${event.location}</p>
                                <p class="event-card-info">${formattedDate}</p>
                                <a href="/events/${event.id}" class="btn-details">Ver Detalhes</a>
                            </div>
                        </div>
                    `;
            eventsGrid.innerHTML += card;
        });

    } catch (error) {
        eventsGrid.innerHTML = '<p>Não foi possível carregar os eventos. Tente novamente mais tarde.</p>';
        console.error('Erro ao buscar eventos:', error);
    }
}