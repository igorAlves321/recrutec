document.getElementById('addVagaForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const titulo = document.getElementById('titulo').value;
    const descricao = document.getElementById('descricao').value;

    // Chamada para o backend (API) para cadastrar a vaga
    fetch('/vagas?recrutadorId=4', {  // Substitua '4' pelo ID do recrutador logado
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            titulo: titulo,
            descricao: descricao,
            status: 'Aberta',  // A vaga sempre começa com o status "Aberta"
        }),
    })
    .then(response => response.json())
    .then(data => {
        console.log('Vaga cadastrada:', data);
        listarVagas();  // Atualiza a lista de vagas
    })
    .catch((error) => {
        console.error('Erro ao cadastrar vaga:', error);
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
                <p>Status: ${vaga.status}</p>
                <button class="btn btn-primary" onclick="editarVaga(${vaga.id})">Editar</button>
                <button class="btn btn-danger" onclick="excluirVaga(${vaga.id})">Excluir</button>
            `;
            vagaList.appendChild(vagaItem);
        });
    })
    .catch((error) => {
        console.error('Erro ao listar vagas:', error);
    });
}

function editarVaga(id) {
    fetch(`/vagas/${id}`)
    .then(response => response.json())
    .then(vaga => {
        document.getElementById('editVagaId').value = vaga.id;
        document.getElementById('editTitulo').value = vaga.titulo;
        document.getElementById('editDescricao').value = vaga.descricao;
        document.getElementById('editStatus').value = vaga.status;
        const editModal = new bootstrap.Modal(document.getElementById('editVagaModal'));
        editModal.show();
    })
    .catch((error) => {
        console.error('Erro ao buscar vaga para edição:', error);
    });
}

function salvarAlteracoesVaga() {
    const id = document.getElementById('editVagaId').value;
    const titulo = document.getElementById('editTitulo').value;
    const descricao = document.getElementById('editDescricao').value;
    const status = document.getElementById('editStatus').value;

    fetch(`/vagas/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            titulo: titulo,
            descricao: descricao,
            status: status,
        }),
    })
    .then(() => {
        listarVagas();  // Atualiza a lista de vagas
        const editModal = bootstrap.Modal.getInstance(document.getElementById('editVagaModal'));
        editModal.hide();
    })
    .catch((error) => {
        console.error('Erro ao salvar alterações da vaga:', error);
    });
}

function excluirVaga(id) {
    fetch(`/vagas/${id}`, {
        method: 'DELETE',
    })
    .then(() => {
        listarVagas();  // Atualiza a lista de vagas
    })
    .catch((error) => {
        console.error('Erro ao excluir vaga:', error);
    });
}

// Inicializa a lista de vagas ao carregar a página
listarVagas();
