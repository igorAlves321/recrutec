/**
 * Script para gerenciar login e registro de usuários
 * Integrado com a API REST padronizada do back-end
 */

// Configuração da API base
const API_BASE_URL = 'http://localhost:8080/api';

// Utilitários para gerenciar tokens JWT
const TokenManager = {
    setTokens: function(accessToken, refreshToken, expiresIn, userInfo) {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('tokenExpiration', Date.now() + (expiresIn * 1000));
        localStorage.setItem('userInfo', JSON.stringify(userInfo));
    },

    getAccessToken: function() {
        return localStorage.getItem('accessToken');
    },

    getRefreshToken: function() {
        return localStorage.getItem('refreshToken');
    },

    getUserInfo: function() {
        const userInfo = localStorage.getItem('userInfo');
        return userInfo ? JSON.parse(userInfo) : null;
    },

    clearTokens: function() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('tokenExpiration');
        localStorage.removeItem('userInfo');
    },

    isTokenExpired: function() {
        const expiration = localStorage.getItem('tokenExpiration');
        return expiration ? Date.now() > parseInt(expiration) : true;
    }
};

// Função para fazer requisições autenticadas
async function authenticatedFetch(url, options = {}) {
    const token = TokenManager.getAccessToken();

    if (!token || TokenManager.isTokenExpired()) {
        // Tentar renovar o token
        const refreshed = await refreshAccessToken();
        if (!refreshed) {
            redirectToLogin();
            return;
        }
    }

    options.headers = {
        ...options.headers,
        'Authorization': `Bearer ${TokenManager.getAccessToken()}`,
        'Content-Type': 'application/json'
    };

    return fetch(url, options);
}

// Função para renovar access token
async function refreshAccessToken() {
    const refreshToken = TokenManager.getRefreshToken();

    if (!refreshToken) {
        return false;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ refreshToken: refreshToken })
        });

        if (response.ok) {
            const data = await response.json();
            TokenManager.setTokens(data.accessToken, data.refreshToken, data.expiresIn, data.user);
            return true;
        }
    } catch (error) {
        console.error('Erro ao renovar token:', error);
    }

    return false;
}

// Função para redirecionar para login
function redirectToLogin() {
    TokenManager.clearTokens();
    window.location.href = 'login.html';
}

// Event listener para formulário de login
if (document.getElementById('loginForm')) {
    document.getElementById('loginForm').addEventListener('submit', async function(event) {
        event.preventDefault();

        const email = document.getElementById('email').value;
        const senha = document.getElementById('senha').value;

        // Remove a seleção de tipo de usuário - o back-end determina automaticamente
        // const userType = document.getElementById('userType').value;

        try {
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email: email, senha: senha }),
            });

            if (response.ok) {
                const data = await response.json();
                console.log('Login bem-sucedido:', data);

                // Armazena os tokens JWT
                TokenManager.setTokens(data.accessToken, data.refreshToken, data.expiresIn, data.user);

                // Redireciona baseado no role do usuário
                const userRole = data.user.role;

                if (userRole === 'ADMIN') {
                    window.location.href = "admin.html";
                } else if (userRole === 'RECRUTADOR') {
                    window.location.href = "gerenciar-vagas.html";
                } else if (userRole === 'CANDIDATO') {
                    window.location.href = "area.html";
                } else {
                    console.error('Role de usuário desconhecido:', userRole);
                    alert('Erro: Tipo de usuário não reconhecido');
                }
            } else {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Login inválido');
            }
        } catch (error) {
            console.error('Erro ao fazer login:', error);
            alert('Login falhou: ' + error.message);
        }
    });
}

// Função para logout
function logout() {
    const refreshToken = TokenManager.getRefreshToken();

    if (refreshToken) {
        fetch(`${API_BASE_URL}/auth/logout`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ refreshToken: refreshToken })
        }).catch(error => {
            console.error('Erro ao fazer logout:', error);
        });
    }

    TokenManager.clearTokens();
    window.location.href = 'login.html';
}

// Função para verificar se usuário está autenticado
function checkAuthentication() {
    const userInfo = TokenManager.getUserInfo();

    if (!userInfo || TokenManager.isTokenExpired()) {
        redirectToLogin();
        return false;
    }

    return true;
}

// Função para exibir informações do usuário logado
function displayUserInfo() {
    const userInfo = TokenManager.getUserInfo();

    if (userInfo) {
        const userInfoElement = document.getElementById('userInfo');
        if (userInfoElement) {
            userInfoElement.innerHTML = `
                <span>Bem-vindo, ${userInfo.nome}!</span>
                <span>(${userInfo.role})</span>
                <button onclick="logout()" class="btn btn-sm btn-outline-danger ms-2">Sair</button>
            `;
        }
    }
}

// Inicializar quando a página carregar
document.addEventListener('DOMContentLoaded', function() {
    // Verificar autenticação em páginas protegidas (exceto login e registro)
    const currentPage = window.location.pathname.split('/').pop();
    const publicPages = ['login.html', 'cadastrar-candidato.html', 'cadastrar-recrutador.html', 'index.html', ''];

    if (!publicPages.includes(currentPage)) {
        checkAuthentication();
        displayUserInfo();
    }
});

