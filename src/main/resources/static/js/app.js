// Funções para carregar dados do backend
async function getCandidatos() {
    const response = await fetch('http://localhost:8080/candidatos');
    return await response.json();
}

async function getRecrutadores() {
    const response = await fetch('http://localhost:8080/recrutadores');
    return await response.json();
}

async function getVagas() {
    const response = await fetch('http://localhost:8080/vagas');
    return await response.json();
}

// Funções de exclusão
async function excluirCandidato(id) {
    const confirmed = confirm('Tem certeza que deseja excluir este candidato?');
    if (confirmed) {
        const response = await fetch(`http://localhost:8080/candidatos/${id}`, {
            method: 'DELETE'
        });
        if (response.ok) {
            alert('Candidato excluído com sucesso!');
            carregarCandidatos(); // Recarregar a lista de candidatos
        }
    }
}

async function excluirRecrutador(id) {
    const confirmed = confirm('Tem certeza que deseja excluir este recrutador?');
    if (confirmed) {
        const response = await fetch(`http://localhost:8080/recrutadores/${id}`, {
            method: 'DELETE'
        });
        if (response.ok) {
            alert('Recrutador excluído com sucesso!');
            carregarRecrutadores(); // Recarregar a lista de recrutadores
        }
    }
}

async function excluirVaga(id) {
    const confirmed = confirm('Tem certeza que deseja excluir esta vaga?');
    if (confirmed) {
        const response = await fetch(`http://localhost:8080/vagas/${id}`, {
            method: 'DELETE'
        });
        if (response.ok) {
            alert('Vaga excluída com sucesso!');
            carregarVagas(); // Recarregar a lista de vagas
        }
    }
}

// Função para atualizar dados
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
