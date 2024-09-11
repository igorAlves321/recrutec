document.getElementById('addCandidatoForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const nome = document.getElementById('nome').value;
    const email = document.getElementById('email').value;

    // Chamada para o backend (API) para cadastrar o candidato
    fetch('/candidatos', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            nome: nome,
            email: email,
        }),
    })
    .then(response => response.json())
    .then(data => {
        console.log('Candidato cadastrado:', data);
        // Atualizar a lista de candidatos
        listarCandidatos();
    })
    .catch((error) => {
        console.error('Erro:', error);
    });
});

function listarCandidatos() {
    fetch('/candidatos')
    .then(response => response.json())
    .then(data => {
        const candidatoList = document.getElementById('candidatoList');
        candidatoList.innerHTML = '';

        data.forEach(candidato => {
            const candidatoItem = document.createElement('div');
            candidatoItem.classList.add('candidato-item');
            candidatoItem.innerHTML = `
                <h4>${candidato.nome}</h4>
                <p>${candidato.email}</p>
                <button class="btn btn-primary" onclick="editarCandidato(${candidato.id})">Editar</button>
                <button class="btn btn-danger" onclick="excluirCandidato(${candidato.id})">Excluir</button>
            `;
            candidatoList.appendChild(candidatoItem);
        });
    });
}

function editarCandidato(id) {
    fetch(`/candidatos/${id}`)
    .then(response => response.json())
    .then(candidato => {
        document.getElementById('editCandidatoId').value = candidato.id;
        document.getElementById('editNome').value = candidato.nome;
        document.getElementById('editEmail').value = candidato.email;
        const editModal = new bootstrap.Modal(document.getElementById('editCandidatoModal'));
        editModal.show();
    });
}

function salvarAlteracoesCandidato() {
    const id = document.getElementById('editCandidatoId').value;
    const nome = document.getElementById('editNome').value;
    const email = document.getElementById('editEmail').value;

    fetch(`/candidatos/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            nome: nome,
            email: email,
        }),
    })
    .then(() => {
        listarCandidatos();
        const editModal = new bootstrap.Modal(document.getElementById('editCandidatoModal'));
        editModal.hide();
    });
}

function excluirCandidato(id) {
    fetch(`/candidatos/${id}`, {
        method: 'DELETE',
    })
    .then(() => {
        listarCandidatos();
    });
}

// Inicializa a lista de candidatos
listarCandidatos();
