// Função para exibir a saudação
function exibirSaudacao() {
    const nomeUsuario = localStorage.getItem('nomeUsuario'); // Obtém o nome do usuário do localStorage
    if (nomeUsuario) {
        document.getElementById('saudacao').innerText = `Olá, ${nomeUsuario}`;
    } else {
        window.location.href = 'login.html'; // Se o nome não estiver no localStorage, redireciona para login
    }
}

// Função para sair (logout)
function sair() {
    localStorage.removeItem('nomeUsuario'); // Remove o nome do usuário do localStorage
    localStorage.removeItem('token'); // Remove o token de autenticação (se estiver usando JWT)
    window.location.href = 'login.html'; // Redireciona para a página de login
}

// Configura o botão de sair
document.getElementById('btnSair').addEventListener('click', sair);

// Chama a função de exibição da saudação quando a página carrega
exibirSaudacao();
