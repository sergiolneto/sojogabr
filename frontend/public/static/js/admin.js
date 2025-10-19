document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('create-champ-form');
    const pendingList = document.getElementById('pending-users-list');

    checkAuth();
    loadPendingUsers();

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('champ-name').value.trim();
        const sport = document.getElementById('champ-sport').value.trim();
        const teamsRaw = document.getElementById('champ-teams').value.trim();
        const teamIds = teamsRaw.split(',').map(s => s.trim()).filter(Boolean);

        if (teamIds.length < 2) {
            Swal.fire('Erro', 'Informe pelo menos 2 IDs de times.', 'error');
            return;
        }

        try {
            const token = localStorage.getItem('token');
            const resp = await fetch('/api/campeonatos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(token ? { 'Authorization': 'Bearer ' + token } : {})
                },
                body: JSON.stringify({ nome: name, esporte: sport, timeIds: teamIds })
            });

            if (resp.ok) {
                Swal.fire('Sucesso', 'Campeonato criado com sucesso.', 'success');
                form.reset();
            } else {
                const err = await resp.json().catch(()=>({message:'Erro'}));
                Swal.fire('Erro', err.message || 'Falha ao criar campeonato', 'error');
            }
        } catch (e) {
            console.error(e);
            Swal.fire('Erro', 'Erro de rede. Tente novamente.', 'error');
        }
    });

    document.getElementById('logout-link').addEventListener('click', (e)=>{
        e.preventDefault();
        localStorage.removeItem('token');
        window.location.href = '/';
    });

    async function loadPendingUsers() {
        try {
            const token = localStorage.getItem('token');
            const resp = await fetch('/api/admin/users/pending', { headers: { ...(token ? { 'Authorization': 'Bearer ' + token } : {}) } });
            if (!resp.ok) {
                pendingList.innerText = 'Não foi possível carregar usuários pendentes.';
                return;
            }
            const users = await resp.json();
            if (!users.length) {
                pendingList.innerText = 'Nenhum usuário pendente.';
                return;
            }

            pendingList.innerHTML = '';
            users.forEach(u => {
                const div = document.createElement('div');
                div.className = 'pending-user';
                div.innerHTML = `
                    <strong>${u.username}</strong> - ${u.email || ''}
                    <button data-approve="${u.username}">Aprovar</button>
                    <button data-promote="${u.username}">Tornar PowerUser</button>
                `;
                pendingList.appendChild(div);
            });

            pendingList.addEventListener('click', async (ev) => {
                const ap = ev.target.getAttribute('data-approve');
                const pr = ev.target.getAttribute('data-promote');
                const token = localStorage.getItem('token');
                if (ap) {
                    const r = await fetch(`/api/admin/users/${encodeURIComponent(ap)}/approve`, { method: 'POST', headers: { ...(token ? { 'Authorization': 'Bearer ' + token } : {}) } });
                    if (r.ok) { Swal.fire('Ok', 'Usuário aprovado', 'success'); loadPendingUsers(); }
                    else Swal.fire('Erro', 'Falha ao aprovar', 'error');
                }
                if (pr) {
                    const r = await fetch(`/api/admin/users/${encodeURIComponent(pr)}/promote`, { method: 'POST', headers: { ...(token ? { 'Authorization': 'Bearer ' + token } : {}) } });
                    if (r.ok) { Swal.fire('Ok', 'Usuário promovido', 'success'); loadPendingUsers(); }
                    else Swal.fire('Erro', 'Falha ao promover', 'error');
                }
            });
        } catch (e) {
            console.error(e);
            pendingList.innerText = 'Erro ao carregar usuários pendentes.';
        }
    }

    function checkAuth() {
        const token = localStorage.getItem('token');
        if (!token) {
            Swal.fire('Acesso', 'Você precisa estar logado como ADMIN para acessar esta página.', 'warning').then(()=>{ window.location.href = '/'; });
        }
    }
});
