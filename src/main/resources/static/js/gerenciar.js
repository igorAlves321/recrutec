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
        const nome = document.getElementById('editNome') ? document.getElementById('editNome').value : null;
        const email = document.getElementById('editEmail') ? document.getElementById('editEmail').value : null;
        const empresa = document.getElementById('editEmpresa') ? document.getElementById('editEmpresa').value : null;
        const titulo = document.getElementById('editTitulo') ? document.getElementById('editTitulo').value : null;
        const descricao = document.getElementById('editDescricao') ? document.getElementById('editDescricao').value : null;
        const status = document.getElementById('editStatus') ? document.getElementById('editStatus').value : null;
        const recrutadorId = document.getElementById('editRecrutadorId') ? document.getElementById('editRecrutadorId').value : null;
        const tipo = document.getElementById('editForm').getAttribute('data-tipo');

        let data;

        // Construção dos dados baseados no tipo de entidade
        if (tipo === 'candidato') {
            data = { nome, email };
        } else if (tipo === 'recrutador') {
            data = { nome, email, empresa };
        } else if (tipo === 'vaga') {
            data = { titulo, descricao, status, recrutadorId };
        }

        atualizarEntidade(id, data, tipo, recrutadorId).then(() => {
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
                            <button class="btn btn-sm btn-warning" onclick="abrirModalEditar(${candidato.id}, '${candidato.nome}', '${candidato.email}', '', 'candidato')">Editar</button>
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
            let html = '<h2>Vagas</h2><table class="table table-striped"><thead><tr><th>Título</th><th>Descrição</th><th>Status</th><th>Data de Postagem</th><th>Ações</th></tr></thead><tbody>';
            data.forEach(vaga => {
                html += `<tr><td>${vaga.titulo}</td><td>${vaga.descricao}</td><td>${vaga.status}</td><td>${vaga.dataPostagem}</td>
                         <td>
                            <button class="btn btn-sm btn-warning" onclick="abrirModalEditar(${vaga.id}, '${vaga.titulo}', '${vaga.descricao}', '${vaga.status}', '${vaga.dataPostagem}', 'vaga')">Editar</button>
                            <button class="btn btn-sm btn-danger" onclick="excluirEntidade(${vaga.id}, 'vaga')">Excluir</button>
                         </td></tr>`;
            });
            html += '</tbody></table>';
            document.getElementById('content').innerHTML = html;
        });
}

// Função para abrir o modal de edição
function abrirModalEditar(id, titulo = '', descricao = '', status = '', dataPostagem = '', tipo) {
    document.getElementById('editId').value = id;
    document.getElementById('editTitulo').value = titulo;
    document.getElementById('editDescricao').value = descricao;
    document.getElementById('editStatus').value = status;
    
    // Exibe a data de postagem, mas não permite edição
    document.getElementById('editDataPostagem').value = dataPostagem;
    document.getElementById('editDataPostagem').setAttribute('disabled', 'disabled');

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
async function atualizarEntidade(id, data, tipo, recrutadorId = null) {
    let pluralTipo = tipo === 'recrutador' ? 'recrutadores' : `${tipo}s`; // Corrige pluralização correta para 'recrutador'
    let endpoint = `http://localhost:8080/${pluralTipo}/${id}`; // Agora usa 'recrutadores' corretamente

    if (tipo === 'vaga' && recrutadorId) {
        endpoint += `?recrutadorId=${recrutadorId}`;
    }

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
