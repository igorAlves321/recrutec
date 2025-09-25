/**
 * Script para cadastro de candidatos
 * Integrado com a API REST do back-end
 */

// Event listener para o formulário de cadastro de candidato
document.getElementById('candidatoForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    // Coleta os dados do formulário
    const formData = {
        nome: document.getElementById('nome').value.trim(),
        email: document.getElementById('email').value.trim(),
        telefone: document.getElementById('telefone').value.trim(),
        senha: document.getElementById('senha').value,
        confirmarSenha: document.getElementById('confirmarSenha').value,
        curriculo: document.getElementById('curriculo').value.trim(),
        pcd: document.getElementById('pcd').value,
        areaInteresse: parseMultipleValues(document.getElementById('areaInteresse').value),
        habilidades: parseMultipleValues(document.getElementById('habilidades').value),
        certificados: parseMultipleValues(document.getElementById('certificados').value)
    };

    // Validações do lado cliente
    if (!validateCandidatoForm(formData)) {
        return;
    }

    // Mostrar loading
    showLoading();

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register/candidato`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        if (response.ok) {
            console.log('Candidato registrado com sucesso:', data);

            // Mostrar mensagem de sucesso
            showSuccessMessage('Conta criada com sucesso!',
                'Seu cadastro foi realizado. Você será redirecionado para a página de login.');

            // Redirecionar para login após 2 segundos
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);

        } else {
            throw new Error(data.message || 'Erro ao criar conta');
        }

    } catch (error) {
        console.error('Erro ao cadastrar candidato:', error);
        showErrorMessage('Erro ao criar conta', error.message);
    } finally {
        hideLoading();
    }
});

/**
 * Converte uma string com valores separados por vírgula em array
 * Remove espaços em branco e valores vazios
 */
function parseMultipleValues(value) {
    if (!value || value.trim() === '') {
        return [];
    }

    return value.split(',')
        .map(item => item.trim())
        .filter(item => item.length > 0);
}

/**
 * Valida os dados do formulário de candidato
 */
function validateCandidatoForm(data) {
    // Validação de nome
    if (data.nome.length < 2) {
        showErrorMessage('Erro de validação', 'Nome deve ter pelo menos 2 caracteres');
        return false;
    }

    // Validação de email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(data.email)) {
        showErrorMessage('Erro de validação', 'Por favor, insira um email válido');
        return false;
    }

    // Validação de telefone (básica)
    if (data.telefone.length < 10) {
        showErrorMessage('Erro de validação', 'Por favor, insira um telefone válido');
        return false;
    }

    // Validação de senha
    if (data.senha.length < 6) {
        showErrorMessage('Erro de validação', 'Senha deve ter pelo menos 6 caracteres');
        return false;
    }

    // Validação de confirmação de senha
    if (data.senha !== data.confirmarSenha) {
        showErrorMessage('Erro de validação', 'Senhas não coincidem');
        return false;
    }

    // Validação de área de interesse
    if (data.areaInteresse.length === 0) {
        showErrorMessage('Erro de validação', 'Por favor, informe pelo menos uma área de interesse');
        return false;
    }

    // Validação de currículo (se informado, deve ser uma URL válida)
    if (data.curriculo && !isValidUrl(data.curriculo)) {
        showErrorMessage('Erro de validação', 'Por favor, insira um link válido para o currículo');
        return false;
    }

    return true;
}

/**
 * Valida se uma string é uma URL válida
 */
function isValidUrl(string) {
    try {
        new URL(string);
        return true;
    } catch (_) {
        return false;
    }
}

/**
 * Mostra overlay de loading
 */
function showLoading() {
    document.getElementById('loadingOverlay').classList.remove('d-none');
    document.getElementById('loadingOverlay').classList.add('d-flex');
}

/**
 * Esconde overlay de loading
 */
function hideLoading() {
    document.getElementById('loadingOverlay').classList.add('d-none');
    document.getElementById('loadingOverlay').classList.remove('d-flex');
}

/**
 * Mostra mensagem de sucesso
 */
function showSuccessMessage(title, message) {
    alert(`${title}\n\n${message}`);
    // TODO: Implementar modal de sucesso mais elegante
}

/**
 * Mostra mensagem de erro
 */
function showErrorMessage(title, message) {
    alert(`${title}\n\n${message}`);
    // TODO: Implementar modal de erro mais elegante
}

// Formatação automática do telefone
document.getElementById('telefone').addEventListener('input', function(e) {
    let value = e.target.value.replace(/\D/g, '');

    if (value.length >= 11) {
        value = value.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    } else if (value.length >= 7) {
        value = value.replace(/(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
    } else if (value.length >= 3) {
        value = value.replace(/(\d{2})(\d{0,5})/, '($1) $2');
    }

    e.target.value = value;
});