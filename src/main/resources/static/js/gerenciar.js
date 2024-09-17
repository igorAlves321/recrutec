document.addEventListener('DOMContentLoaded', function () {
    const userType = sessionStorage.getItem('userType');
    const userId = sessionStorage.getItem('userId'); // Obtém o ID do usuário logado

    if (userType !== 'admin' && userType !== 'recrutador') {
        alert('Acesso negado. Somente administradores ou recrutadores podem acessar esta página.');
        window.location.href = 'index.html';  // Redireciona para a página inicial
    }

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
        console.log("ID passado para o PUT:", id); // <-- Verificação do ID

        const nome = document.getElementById('editNome') ? document.getElementById('editNome').value : null;
        const email = document.getElementById('editEmail') ? document.getElementById('editEmail').value : null;
        const empresa = document.getElementById('editEmpresa') ? document.getElementById('editEmpresa').value : null;
        const titulo = document.getElementById('editTitulo') ? document.getElementById('editTitulo').value : null;
        const descricao = document.getElementById('editDescricao') ? document.getElementById('editDescricao').value : null;
        const status = document.getElementById('editStatus') ? document.getElementById('editStatus').value : null;

        let recrutadorId = document.getElementById('editRecrutadorId') ? document.getElementById('editRecrutadorId').value : null;

        const tipo = document.getElementById('editForm').getAttribute('data-tipo');
        console.log("Tipo obtido do formulário:", tipo); // <-- Verificação do tipo

        let data;

        if (tipo === 'vaga') {
            if (!recrutadorId) {
                recrutadorId = userId; // Se o recrutadorId não estiver definido, usa o ID do usuário logado
            }
            data = { titulo, descricao, status, recrutador: { id: recrutadorId } };
        } else if (tipo === 'candidato') {
            data = { nome, email };
        } else if (tipo === 'recrutador') {
            data = { nome, email, empresa };
        }

        if (tipo) {
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
        } else {
            console.error("Tipo indefinido!");
        }
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
                            <button class="btn btn-sm btn-warning" onclick="abrirModalEditar(${candidato.id}, '${candidato.nome}', '${candidato.email}', '', '', '', '', '', 'candidato')">Editar</button>
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
                            <button class="btn btn-sm btn-warning" onclick="abrirModalEditar(${recrutador.id}, '${recrutador.nome}', '${recrutador.email}', '${recrutador.empresa}', '', '', '', '', 'recrutador')">Editar</button>
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
                            <button class="btn btn-sm btn-warning" onclick="abrirModalEditar(${vaga.id}, '', '', '', '${vaga.titulo}', '${vaga.descricao}', '${vaga.status}', '${vaga.dataPostagem}', 'vaga')">Editar</button>
                            <button class="btn btn-sm btn-danger" onclick="excluirEntidade(${vaga.id}, 'vaga')">Excluir</button>
                         </td></tr>`;
            });
            html += '</tbody></table>';
            document.getElementById('content').innerHTML = html;
        });
}

// Função para abrir o modal de edição com logs detalhados
function abrirModalEditar(id, nome = '', email = '', empresa = '', titulo = '', descricao = '', status = '', dataPostagem = '', tipo) {
    // Adicionando logs para verificar os valores recebidos
    console.log("ID recebido:", id);
    console.log("Nome recebido:", nome);
    console.log("Email recebido:", email);
    console.log("Empresa recebida:", empresa);
    console.log("Título recebido:", titulo);
    console.log("Descrição recebida:", descricao);
    console.log("Status recebido:", status);
    console.log("Data de Postagem recebida:", dataPostagem);
    console.log("Tipo recebido:", tipo);

    // Verifica se o tipo foi passado corretamente
    if (!tipo) {
        console.error("Tipo inválido! Nenhum tipo foi passado.");
        alert("Erro: tipo indefinido. Verifique se o tipo correto está sendo passado.");
        return;
    }

    // Define o ID no formulário
    document.getElementById('editId').value = id;

    // Define o tipo no formulário
    document.getElementById('editForm').setAttribute('data-tipo', tipo);
    console.log("Tipo atribuído ao formulário:", tipo);

    // Campos de candidatos, recrutadores e vagas
    const nomeField = document.getElementById('nomeField');
    const emailField = document.getElementById('emailField');
    const empresaField = document.getElementById('empresaField');
    const tituloField = document.getElementById('tituloField');
    const descricaoField = document.getElementById('descricaoField');
    const statusField = document.getElementById('statusField');
    const dataPostagemField = document.getElementById('dataPostagemField');

    const editNome = document.getElementById('editNome');
    const editEmail = document.getElementById('editEmail');
    const editEmpresa = document.getElementById('editEmpresa');
    const editTitulo = document.getElementById('editTitulo');
    const editDescricao = document.getElementById('editDescricao');
    const editStatus = document.getElementById('editStatus');
    const editDataPostagem = document.getElementById('editDataPostagem');

    // Removendo required de todos os campos
    editNome.removeAttribute('required');
    editEmail.removeAttribute('required');
    if (editEmpresa) {
        editEmpresa.removeAttribute('required');
    }

    // Lógica de visibilidade e required dos campos baseado no tipo
    if (tipo === 'candidato') {
        nomeField.style.display = 'block';
        emailField.style.display = 'block';
        editNome.value = nome;
        editEmail.value = email;

        editNome.setAttribute('required', 'true');
        editEmail.setAttribute('required', 'true');

        empresaField.style.display = 'none';
        tituloField.style.display = 'none';
        descricaoField.style.display = 'none';
        statusField.style.display = 'none';
        dataPostagemField.style.display = 'none';

    } else if (tipo === 'recrutador') {
        nomeField.style.display = 'block';
        emailField.style.display = 'block';
        empresaField.style.display = 'block';
        editNome.value = nome;
        editEmail.value = email;
        editEmpresa.value = empresa;

        editNome.setAttribute('required', 'true');
        editEmail.setAttribute('required', 'true');
        editEmpresa.setAttribute('required', 'true');

        tituloField.style.display = 'none';
        descricaoField.style.display = 'none';
        statusField.style.display = 'none';
        dataPostagemField.style.display = 'none';

    } else if (tipo === 'vaga') {
        tituloField.style.display = 'block';
        descricaoField.style.display = 'block';
        statusField.style.display = 'block';
        dataPostagemField.style.display = 'block';
        editTitulo.value = titulo;
        editDescricao.value = descricao;
        editStatus.value = status;
        editDataPostagem.value = dataPostagem;

        nomeField.style.display = 'none';
        emailField.style.display = 'none';
        empresaField.style.display = 'none';

    } else {
        console.error("Tipo inválido detectado: " + tipo);
        alert("Erro: tipo inválido detectado.");
        return;
    }

    // Exibe o modal
    const modal = new bootstrap.Modal(document.getElementById('editModal'));
    modal.show();
}

// Funções de exclusão para candidatos, recrutadores e vagas
function excluirEntidade(id, tipo) {
    const userId = sessionStorage.getItem('userId'); // Obtém o ID do usuário logado
    const userType = sessionStorage.getItem('userType').toUpperCase(); // Obtém o tipo do usuário logado e converte para maiúsculas
    let endpoint;

    // Log dos valores que serão usados
    console.log(`Tentando excluir ${tipo} com ID: ${id}`);
    console.log('userId:', userId, 'userRole:', userType);
    if (tipo === 'vaga' && userType === 'ADMIN') {
        endpoint = `/vagas/${id}`; // Administrador não precisa passar recrutadorId
        console.log("Administrador tentando excluir a vaga:", endpoint);
    } else {
        endpoint = {
            candidato: `/candidatos/${id}`,
            recrutador: `/recrutadores/${id}?role=${userType}`,
            vaga: `/vagas/${id}?userId=${userId}&userRole=${userType}` // Passa o userId e userRole na URL
        }[tipo];
        console.log("Recrutador ou outro tipo de usuário tentando excluir:", endpoint);
    }

    if (confirm(`Tem certeza que deseja excluir este ${tipo}?`)) {
        console.log(`Enviando requisição DELETE para ${endpoint}`);

        fetch(endpoint, {
            method: 'DELETE',
        })
        .then(response => {
            console.log('Resposta da exclusão:', response.status); // Log do status da resposta
            if (response.ok) {
                console.log(`${tipo} excluído com sucesso!`);
                if (tipo === 'candidato') {
                    carregarCandidatos();
                } else if (tipo === 'recrutador') {
                    carregarRecrutadores();
                } else if (tipo === 'vaga') {
                    carregarVagas();
                }
            } else {
                console.error(`Erro ao excluir ${tipo}. Status:`, response.status);
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
    if (!tipo) {
        console.error("Tipo indefinido!");
        return;
    }

    // Define o plural do tipo corretamente
    let pluralTipo;
    if (tipo === 'recrutador') {
        pluralTipo = 'recrutadores';
    } else if (tipo === 'candidato') {
        pluralTipo = 'candidatos';
    } else if (tipo === 'vaga') {
        pluralTipo = 'vagas';
    } else {
        console.error("Tipo inválido!");
        return;
    }

    let endpoint = `http://localhost:8080/${pluralTipo}/${id}`; // Usa o tipo correto para construir a URL

    console.log("URL do PUT:", endpoint);  // Verifica a URL
    console.log("Dados enviados no PUT:", JSON.stringify(data));  // Verifica os dados

    try {
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
    } catch (error) {
        console.error("Erro ao atualizar:", error);
        throw error;
    }
}