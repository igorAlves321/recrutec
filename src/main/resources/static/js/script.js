document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;

    // Seleção do tipo de usuário (Recrutador, Candidato ou Admin)
    const userType = document.getElementById('userType').value;  // Pode ser 'recrutador', 'candidato' ou 'admin'

    // Monta a URL correta para recrutador, candidato ou admin
    const loginUrl = `/login/${userType}`;

    fetch(loginUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email: email, senha: senha }),
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Login inválido');
        }
    })
    .then(data => {
        console.log('Login bem-sucedido:', data);
        
        // Armazena o ID e o tipo do usuário logado no localStorage ou sessionStorage
        sessionStorage.setItem('userId', data.id);
        sessionStorage.setItem('userType', userType);  // Pode ser 'admin', 'recrutador', ou 'candidato'

        // Redireciona para a página apropriada de acordo com o tipo de usuário
        if (userType === 'admin') {
            window.location.href = "admin.html";  // Página do Admin
        } else if (userType === 'recrutador') {
            window.location.href = "gerenciar-vagas.html";  // Página do Recrutador
        } else if (userType === 'candidato') {
            window.location.href = "vagas.html";  // Página de Vagas para Candidato
        }
    })
    .catch(error => {
        console.error('Erro ao fazer login:', error);
        alert('Login falhou. Verifique suas credenciais.');
    });
});
