document.addEventListener('DOMContentLoaded', function () {
    // Adicionar eventos para os menus
    document.getElementById('menuCandidatos').addEventListener('click', function (event) {
        event.preventDefault();
        carregarEntidades('candidato');
    });

    document.getElementById('menuRecrutadores').addEventListener('click', function (event) {
        event.preventDefault();
        carregarEntidades('recrutador');
    });

    document.getElementById('menuVagas').addEventListener('click', function (event) {
        event.preventDefault();
        carregarEntidades('vaga');
    });

    // Submissão do formulário de edição
    document.getElementById('editForm').addEventListener('submit', function (event) {
        event.preventDefault();
        const id = document.getElementById('editId').value;
        const nome = document.getElementById('editNome').value;
        const email = document.getElementById('editEmail').value;
        const tipo = document.getElementById('editForm').getAttribute('data-tipo');
        const empresa = document.getElementById('editEmpresa')?.value || null;

        let data = { nome, email };
        if (tipo === 'recrutador') {
            data.empresa = empresa;
        }

        atualizarEntidade(id, data, tipo).then(() => {
            alert(`${tipo.charAt(0).toUpperCase() + tipo.slice(1)} atualizado com sucesso!`);
            const modal = bootstrap.Modal.getInstance(document.getElementById('editModal'));
            modal.hide();
            carregarEntidades(tipo);
        }).catch(error => {
            console.error('Erro ao atualizar:', error);
            alert('Erro ao atualizar os dados.');
        });
    });
});

// Função genérica para carregar entidades (candidatos, recrutadores, vagas)
function carregarEntidades(tipo) {
    const endpoint = {
        candidato: '/candidatos',
        recrutador: '/recrutadores',
        vaga: '/vagas'
    }[tipo];

    document.getElementById('content').innerHTML = `<h2>Carregando ${tipo}s...</h2>`;
    
    fetch(endpoint)
        .then(response => response.json())
        .then(data => {
            let html = `<h2>${tipo.charAt(0).toUpperCase() + tipo.slice(1)}s</h2><table class="table table-striped"><thead><tr>`;
            if (tipo === 'vaga') {
                html += '<th>Título</th><th>Descrição</th>';
            } else {
                html += '<th>Nome</th><th>Email</th>';
                if (tipo === 'recrutador') {
                    html += '<th>Empresa</th>';
                }
            }
            html += '<th>Ações</th></tr></thead><tbody>';

            data.forEach(entidade => {
                html += `<tr>`;
                if (tipo === 'vaga') {
                    html += `<td>${entidade.titulo}</td><td>${entidade.descricao}</td>`;
                } else {
                    html += `<td>${entidade.nome}</td><td>${entidade.email}</td>`;
                    if (tipo === 'recrutador') {
                        html += `<td>${entidade.empresa}</td>`;
                    }
                }
                html += `<td>
                            <button class="btn btn-sm btn-warning" onclick="abrirModalEditar(${entidade.id}, '${entidade.nome}', '${entidade.email}', '${entidade.empresa || ''}', '${tipo}')">Editar</button>
                            <button class="btn btn-sm btn-danger" onclick="excluirEntidade(${entidade.id}, '${tipo}')">Excluir</button>
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

// Função genérica para excluir entidades
function excluirEntidade(id, tipo) {
    const endpoint = {
        candidato: `/candidatos/${id}`,
        recrutador: `/recrutadores/${id}`,
        vaga: `/vagas/${id}`
    }[tipo];

    if (confirm(`Tem certeza que deseja excluir este ${tipo}?`)) {
        fetch(endpoint, { method: 'DELETE' })
            .then(response => {
                if (response.ok) {
                    carregarEntidades(tipo);
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

// Função genérica para atualizar uma entidade
async function atualizarEntidade(id, data, tipo) {
    const endpoint = {
        candidato: `http://localhost:8080/candidatos/${id}`,
        recrutador: `http://localhost:8080/recrutadores/${id}`,
        vaga: `http://localhost:8080/vagas/${id}`
    }[tipo];

    const response = await fetch(endpoint, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
    });

    if (!response.ok) {
        throw new Error('Erro ao atualizar os dados');
    }
}
