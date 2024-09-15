// Função para exibir campos específicos dependendo do papel selecionado
document.getElementById('role').addEventListener('change', function () {
    const role = this.value;
    const candidatoFields = document.getElementById('candidatoFields');
    const recrutadorFields = document.getElementById('recrutadorFields');

    // Mostra ou oculta campos baseados no papel selecionado
    if (role === 'CANDIDATO') {
        candidatoFields.style.display = 'block';
        recrutadorFields.style.display = 'none';
    } else if (role === 'RECRUTADOR') {
        recrutadorFields.style.display = 'block';
        candidatoFields.style.display = 'none';
    } else {
        // Esconde ambos se não for Candidato nem Recrutador
        candidatoFields.style.display = 'none';
        recrutadorFields.style.display = 'none';
    }
});

// Função para cadastrar usuário (Candidato ou Recrutador)
document.getElementById('cadastroForm').addEventListener('submit', function (event) {
    event.preventDefault(); // Impede o comportamento padrão do formulário

    const nome = document.getElementById('nome').value;
    const email = document.getElementById('email').value;
    const telefone = document.getElementById('telefone').value;
    const senha = document.getElementById('senha').value;
    const role = document.getElementById('role').value;

    let data = { nome, email, telefone, senha, role };
    let endpoint = '';

    // Se o papel for Candidato, adiciona campos específicos de Candidato
    if (role === 'CANDIDATO') {
        const curriculo = document.getElementById('curriculo').value;
        const areaInteresse = document.getElementById('areaInteresse').value.split(',');
        const pcd = document.getElementById('pcd').value;
        data = { ...data, curriculo, areaInteresse, pcd };
        endpoint = '/candidatos';
    }

    // Se o papel for Recrutador, adiciona campos específicos de Recrutador
    if (role === 'RECRUTADOR') {
        const empresa = document.getElementById('empresa').value;
        data = { ...data, empresa };
        endpoint = '/recrutadores';
    }

    // Envia os dados para o endpoint correto com base no papel
    fetch(`http://localhost:8080${endpoint}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    })
    .then(response => {
        if (response.ok) {
            alert('Usuário cadastrado com sucesso!');
            window.location.href = '/login.html'; // Redireciona após cadastro
        } else {
            alert('Erro ao cadastrar usuário');
        }
    })
    .catch(error => {
        console.error('Erro no cadastro:', error);
        alert('Erro no cadastro');
    });
});
