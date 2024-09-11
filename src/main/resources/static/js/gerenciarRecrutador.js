document.getElementById('addRecrutadorForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const nome = document.getElementById('nome').value;
    const email = document.getElementById('email').value;
    const empresa = document.getElementById('empresa').value;

    // Chamada para o backend (API) para cadastrar o recrutador
    fetch('/recrutadores', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            nome: nome,
            email: email,
            empresa: empresa,
        }),
    })
    .then(response => response.json())
    .then(data => {
        console.log('Recrutador cadastrado:', data);
        // Atualizar a lista de recrutadores
        listarRecrutadores();
    })
    .catch((error) => {
        console.error('Erro:', error);
    });
});

function listarRecrutadores() {
    fetch('/recrutadores')
    .then(response => response.json())
    .then(data => {
        const recrutadorList = document.getElementById('recrutadorList');
        recrutadorList.innerHTML = '';

        data.forEach(recrutador => {
            const recrutadorItem = document.createElement('div');
            recrutadorItem.classList.add('recrutador-item');
            recrutadorItem.innerHTML = `
                <h4>${recrutador.nome}</h4>
                <p>${recrutador.email}</p>
                <p>${recrutador.empresa}</p>
                <button class="btn btn-primary" onclick="editarRecrutador(${recrutador.id})">Editar</button>
                <button class="btn btn-danger" onclick="excluirRecrutador(${recrutador.id})">Excluir</button>
            `;
            recrutadorList.appendChild(recrutadorItem);
        });
    });
}

function editarRecrutador(id) {
    fetch(`/recrutadores/${id}`)
    .then(response => response.json())
    .then(recrutador => {
        document.getElementById('editRecrutadorId').value = recrutador.id;
        document.getElementById('editNome').value = recrutador.nome;
        document.getElementById('editEmail').value = recrutador.email;
        document.getElementById('editEmpresa').value = recrutador.empresa;
        const editModal = new bootstrap.Modal(document.getElementById('editRecrutadorModal'));
        editModal.show();
    });
}

function salvarAlteracoesRecrutador() {
    const id = document.getElementById('editRecrutadorId').value;
    const nome = document.getElementById('editNome').value;
    const email = document.getElementById('editEmail').value;
    const empresa = document.getElementById('editEmpresa').value;

    fetch(`/recrutadores/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            nome: nome,
            email: email,
            empresa: empresa,
        }),
    })
    .then(() => {
        listarRecrutadores();
        const editModal = new bootstrap.Modal(document.getElementById('editRecrutadorModal'));
        editModal.hide();
    });
}

function excluirRecrutador(id) {
    fetch(`/recrutadores/${id}`, {
        method: 'DELETE',
    })
    .then(() => {
        listarRecrutadores();
    });
}

// Inicializa a lista de recrutadores
listarRecrutadores();
