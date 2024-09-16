document.addEventListener('DOMContentLoaded', function () {
    // Seleção dos menus e navegação entre seções
    document.getElementById('menuCandidatos').addEventListener('click', function (event) {
        event.preventDefault();
        carregarCandidatos();
    });

    document.getElementById('menuRecrutadores').addEventListener('click', function (event) {
        event.preventDefault();
        carregarRecrutadores();
    });

    document.getElementById('menuVagas').addEventListener('click', function (event) {
        event.preventDefault();
        carregarVagas();
    });

    // Submissão do formulário de edição
    document.getElementById('editForm').addEventListener('submit', function (event) {
        event.preventDefault();
        const id = document.getElementById('editId').value;
        const nome = document.getElementById('editNome').value;
        const email = document.getElementById('editEmail').value;
        const tipo = document.getElementById('editForm').getAttribute('data-tipo');

        let data = { nome, email };

        atualizarEntidade(id, data, tipo).then(() => {
            alert(`${tipo.charAt(0).toUpperCase() + tipo.slice(1)} atualizado com sucesso!`);
            const modal = bootstrap.Modal.getInstance(document.getElementById('editModal'));
            modal.hide();
            if (tipo === 'candidato') {
                carregarCandidatos();
            } else if (tipo === 'recrutador') {
                carregarRecrutadores();
            } else if (tipo === 'vaga') {
                carregarVagas();
            }
        }).catch(error => {
            console.error('Erro ao atualizar:', error);
            alert('Erro ao atualizar os dados.');
        });
    });
});

// Funções para carregar dados e manipular entidades
function carregarCandidatos() {
    document.getElementById('content').innerHTML = '<h2>Carregando Candidatos...</h2>';
    fetch('/candidatos')
    .then(response => response.json())
    .then(data => {
        let html = '<h2>Candidatos</h2><table class="table table-striped"><thead><tr><th>Nome</th><th>Email</th><th>Ações</th></tr></thead><tbody>';
        data.forEach(candidato => {
            html += `<tr><td>${candidato.nome}</td><td>${candidato.email}</td>
                     <td>
                        <button class="btn btn-sm btn-warning" onclick="abrirModalEditar(${candidato.id}, '${candidato.nome}', '${candidato.email}', 'candidato')">Editar</button>
                        <button class="btn btn-sm btn-danger" onclick="excluirEntidade(${candidato.id}, 'candidato')">Excluir</button>
                     </td></tr>`;
        });
        html += '</tbody></table>';
        document.getElementById('content').innerHTML = html;
    });
}

function carregarRecrutadores() {
    document.getElementById('content').innerHTML = '<h2>Carregando Recrutadores...</h2>';
    fetch('/recrutadores')
    .then(response => response.json())
    .then(data => {
        let html = '<h2>Recrutadores</h2><table class="table table-striped"><thead><tr><th>Nome</th><th>Email</th><th>Empresa</th><th>Ações</th></tr></thead><tbody>';
        data.forEach(recrutador => {
            html += `<tr><td>${recrutador.nome}</td><td>${recrutador.email}</td><td>${recrutador.empresa}</td>
                     <td>
                        <button class="btn btn-sm btn-warning" onclick="abrirModalEditar(${recrutador.id}, '${recrutador.nome}', '${recrutador.email}', '${recrutador.empresa}', 'recrutador')">Editar</button>
                        <button class="btn btn-sm btn-danger" onclick="excluirEntidade(${recrutador.id}, 'recrutador')">Excluir</button>
                     </td></tr>`;
        });
        html += '</tbody></table>';
        document.getElementById('content').innerHTML = html;
    });
}

function carregarVagas() {
    document.getElementById('content').innerHTML = '<h2>Carregando Vagas...</h2>';
    fetch('/vagas')
    .then(response => response.json())
    .then(data => {
        let html = '<h2>Vagas</h2><table class="table table-striped"><thead><tr><th>Título</th><th>Descrição</th><th>Ações</th></tr></thead><tbody>';
        data.forEach(vaga => {
            html += `<tr><td>${vaga.titulo}</td><td>${vaga.descricao}</td>
                     <td>
                        <button class="btn btn-sm btn-warning" onclick="abrirModalEditar(${vaga.id}, '${vaga.titulo}', '${vaga.descricao}', 'vaga')">Editar</button>
                        <button class="btn btn-sm btn-danger" onclick="excluirEntidade(${vaga.id}, 'vaga')">Excluir</button>
                     </td></tr>`;
        });
        html += '</tbody></table>';
        document.getElementById('content').innerHTML = html;
    });
}

// Função para abrir o modal de edição
function abrirModalEditar(id, nome, email, empresa = '', tipo) {
    document.getElementById('editId').value = id;
    document.getElementById('editNome').value = nome;
    document.getElementById('editEmail').value = email;

    const empresaField = document.getElementById('editEmpresa');
    if (empresaField) {
        if (tipo === 'recrutador') {
            empresaField.parentElement.style.display = 'block';
            empresaField.value = empresa;
        } else {
            empresaField.parentElement.style.display = 'none';
        }
    }

    document.getElementById('editForm').setAttribute('data-tipo', tipo);
    const modal = new bootstrap.Modal(document.getElementById('editModal'));
    modal.show();
}

// Funções de exclusão para candidatos, recrutadores e vagas

function excluirEntidade(id, tipo) {
    const endpoint = {
        candidato: `/candidatos/${id}`,
        recrutador: `/recrutadores/${id}`,
        vaga: `/vagas/${id}`
    }[tipo];

    if (confirm(`Tem certeza que deseja excluir este ${tipo}?`)) {
        fetch(endpoint, {
            method: 'DELETE',
        })
        .then(response => {
            if (response.ok) {
                if (tipo === 'candidato') {
                    carregarCandidatos();
                } else if (tipo === 'recrutador') {
                    carregarRecrutadores();
                } else if (tipo === 'vaga') {
                    carregarVagas();
                }
            } else {
                alert(`Erro ao excluir ${tipo}.`);
            }
        })
        .catch(error => {
            console.error(`Erro ao excluir ${tipo}:`, error);
            alert(`Erro ao excluir ${tipo}.`);
        });
    }
}

// Função para atualizar uma entidade (candidato, recrutador ou vaga)
async function atualizarEntidade(id, data, tipo) {
    const endpoint = {
        candidato: `http://localhost:8080/candidatos/${id}`,
        recrutador: `http://localhost:8080/recrutadores/${id}`,
        vaga: `http://localhost:8080/vagas/${id}`
    }[tipo];

    const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    });

    if (!response.ok) {
        throw new Error('Erro ao atualizar os dados');
    }
}
