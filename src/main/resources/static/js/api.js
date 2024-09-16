document.addEventListener('DOMContentLoaded', function () {
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
        const empresa = document.getElementById('editEmpresa').value || null;
        const tipo = document.getElementById('editForm').getAttribute('data-tipo');

        let data = { nome, email };

        if (tipo === 'recrutador') {
            data = { ...data, empresa };
        }

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
