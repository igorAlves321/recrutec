document.getElementById('addVagaForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const titulo = document.getElementById('titulo').value;
    const descricao = document.getElementById('descricao').value;

    // Chamada para o backend (API) para cadastrar a vaga
    fetch('/vagas', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            titulo: titulo,
            descricao: descricao,
        }),
    })
    .then(response => response.json())
    .then(data => {
        console.log('Vaga cadastrada:', data);
        // Atualizar a lista de vagas
        listarVagas();
    })
    .catch((error) => {
        console.error('Erro:', error);
    });
});

function listarVagas() {
    fetch('/vagas')
    .then(response => response.json())
    .then(data => {
        const vagaList = document.getElementById('vagaList');
        vagaList.innerHTML = '';

        data.forEach(vaga => {
            const vagaItem = document.createElement('div');
            vagaItem.classList.add('vaga-item');
            vagaItem.innerHTML = `
                <h4>${vaga.titulo}</h4>
                <p>${vaga.descricao}</p>
                <button class="btn btn-primary" onclick="editarVaga(${vaga.id})">Editar</button>
                <button class="btn btn-danger" onclick="excluirVaga(${vaga.id})">Excluir</button>
            `;
            vagaList.appendChild(vagaItem);
        });
    });
}

function editarVaga(id) {
    fetch(`/vagas/${id}`)
    .then(response => response.json())
    .then(vaga => {
        document.getElementById('editVagaId').value = vaga.id;
        document.getElementById('editTitulo').value = vaga.titulo;
        document.getElementById('editDescricao').value = vaga.descricao;
        const editModal = new bootstrap.Modal(document.getElementById('editVagaModal'));
        editModal.show();
    });
}

function salvarAlteracoesVaga() {
    const id = document.getElementById('editVagaId').value;
    const titulo = document.getElementById('editTitulo').value;
    const descricao = document.getElementById('editDescricao').value;

    fetch(`/vagas/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            titulo: titulo,
            descricao: descricao,
        }),
    })
    .then(() => {
        listarVagas();
        const editModal = new bootstrap.Modal(document.getElementById('editVagaModal'));
        editModal.hide();
    });
}

function excluirVaga(id) {
    fetch(`/vagas/${id}`, {
        method: 'DELETE',
    })
    .then(() => {
        listarVagas();
    });
}

// Inicializa a lista de vagas
listarVagas();
