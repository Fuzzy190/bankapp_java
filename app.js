const API_URL = 'http://localhost:8089/api/bank';

// Session State
let currentAccount = '';
let currentPin = '';
let currentAction = '';

// Screen Navigation
function switchScreen(screenId) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    document.getElementById(screenId).classList.add('active');
    clearMessages();
}

function clearMessages() {
    document.querySelectorAll('.message').forEach(m => {
        m.innerText = '';
        m.className = 'message';
    });
}

// Fetch Accounts for Login Combobox
async function loadLoginAccounts() {
    const loginAccSelect = document.getElementById('login-acc');
    try {
        const response = await fetch(`${API_URL}/accounts`);
        if (response.ok) {
            const accounts = await response.json();
            loginAccSelect.innerHTML = '<option value="">Select Account</option>';
            
            if (accounts.length === 0) {
                loginAccSelect.innerHTML = '<option value="" disabled>No accounts available - Please create one</option>';
            } else {
                accounts.forEach(acc => {
                    loginAccSelect.innerHTML += `<option value="${acc.accountNumber}">${acc.accountName} (${acc.accountNumber})</option>`;
                });
            }
        } else {
            loginAccSelect.innerHTML = '<option value="">Error loading accounts</option>';
        }
    } catch (error) {
        loginAccSelect.innerHTML = '<option value="">Server offline. Start Spring Boot.</option>';
    }
}

// Authentication & Account Creation
async function login() {
    const acc = document.getElementById('login-acc').value;
    const pin = document.getElementById('login-pin').value;
    const msg = document.getElementById('login-msg');

    if (!acc || !pin) {
        msg.innerText = "Please select an account and enter PIN.";
        msg.classList.add('error');
        return;
    }

    try {
        const response = await fetch(`${API_URL}/balance`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ accountNumber: acc, pin: pin })
        });

        const data = await response.text();

        if (response.ok) {
            currentAccount = acc;
            currentPin = pin;
            document.getElementById('dash-acc-num').innerText = acc;
            refreshBalance();
            switchScreen('dashboard-screen');
        } else {
            msg.innerText = data;
            msg.classList.add('error');
        }
    } catch (error) {
        msg.innerText = "Cannot connect to server.";
        msg.classList.add('error');
    }
}

async function createAccount() {
    const name = document.getElementById('create-name').value;
    const pin = document.getElementById('create-pin').value;
    const deposit = document.getElementById('create-deposit').value;
    const msg = document.getElementById('create-msg');

    try {
        const response = await fetch(`${API_URL}/accounts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ accountName: name, pin: pin, initialDeposit: deposit || 0 })
        });

        if (response.ok) {
            const data = await response.json();
            alert(`Success! Your Account Number is: ${data.accountNumber}\nPlease save this number.`);
            
            // Refresh login combobox so the new account appears immediately
            await loadLoginAccounts();
            
            // Clear creation fields
            document.getElementById('create-name').value = '';
            document.getElementById('create-pin').value = '';
            document.getElementById('create-deposit').value = '';
            
            switchScreen('login-screen');
        } else {
            msg.innerText = await response.text();
            msg.classList.add('error');
        }
    } catch (error) {
        msg.innerText = "Cannot connect to server.";
        msg.classList.add('error');
    }
}

function logout() {
    currentAccount = '';
    currentPin = '';
    document.getElementById('login-acc').value = '';
    document.getElementById('login-pin').value = '';
    switchScreen('login-screen');
}

// Dashboard Actions
async function refreshBalance() {
    const response = await fetch(`${API_URL}/balance`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accountNumber: currentAccount, pin: currentPin })
    });
    const data = await response.text();
    const balance = data.split('P')[1];
    document.getElementById('dash-balance').innerText = `₱${balance}`;
}

async function showAction(action) {
    currentAction = action;
    document.getElementById('action-buttons').classList.add('hidden');
    document.getElementById('action-panel').classList.remove('hidden');
    document.getElementById('action-dest').classList.add('hidden');
    document.getElementById('action-amount').value = '';
    clearMessages();

    const title = document.getElementById('action-title');
    if (action === 'deposit') title.innerText = 'CASH DEPOSIT';
    if (action === 'withdraw') title.innerText = 'CASH WITHDRAWAL';
    
    // Setup combobox for transfer
    if (action === 'transfer') {
        title.innerText = 'FUND TRANSFER';
        const destSelect = document.getElementById('action-dest');
        destSelect.classList.remove('hidden');
        destSelect.innerHTML = '<option value="">Loading accounts...</option>';

        try {
            const response = await fetch(`${API_URL}/accounts`);
            if (response.ok) {
                const accounts = await response.json();
                destSelect.innerHTML = '<option value="">Select Destination Account</option>';
                
                accounts.forEach(acc => {
                    // Do not allow transferring to their own account
                    if (acc.accountNumber !== currentAccount) {
                        destSelect.innerHTML += `<option value="${acc.accountNumber}">${acc.accountName} (${acc.accountNumber})</option>`;
                    }
                });
            } else {
                destSelect.innerHTML = '<option value="">Error loading accounts</option>';
            }
        } catch (error) {
            destSelect.innerHTML = '<option value="">Error loading accounts</option>';
        }
    }

    document.getElementById('action-submit').onclick = executeAction;
}

function hideAction() {
    document.getElementById('action-panel').classList.add('hidden');
    document.getElementById('history-panel').classList.add('hidden');
    document.getElementById('action-buttons').classList.remove('hidden');
    refreshBalance();
}

async function executeAction() {
    const amount = document.getElementById('action-amount').value;
    const dest = document.getElementById('action-dest').value;
    const msg = document.getElementById('action-msg');
    
    let endpoint = `${API_URL}/${currentAction}`;
    let payload = {
        accountNumber: currentAccount,
        pin: currentPin,
        amount: amount
    };

    if (currentAction === 'transfer') {
        if (!dest) {
            msg.innerText = "Please select a destination account.";
            msg.className = 'message error';
            return;
        }
        payload.destinationAccountNumber = dest;
    }

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.text();

        if (response.ok) {
            msg.innerText = "Transaction Successful!";
            msg.className = 'message success';
            setTimeout(hideAction, 2000);
        } else {
            msg.innerText = data;
            msg.className = 'message error';
        }
    } catch (error) {
        msg.innerText = "Network Error.";
        msg.className = 'message error';
    }
}

// History
async function loadHistory() {
    document.getElementById('action-buttons').classList.add('hidden');
    document.getElementById('history-panel').classList.remove('hidden');
    
    const tbody = document.getElementById('history-body');
    tbody.innerHTML = '<tr><td colspan="3">Loading...</td></tr>';

    try {
        const response = await fetch(`${API_URL}/history`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ accountNumber: currentAccount, pin: currentPin })
        });

        if (response.ok) {
            const data = await response.json();
            tbody.innerHTML = '';
            data.forEach(tx => {
                tbody.innerHTML += `
                    <tr>
                        <td>${tx.transactionType}</td>
                        <td>₱${tx.amount}</td>
                        <td>₱${tx.balanceAfter}</td>
                    </tr>
                `;
            });
        }
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="3">Error loading history</td></tr>';
    }
}

// Initialize the app by fetching accounts for the login dropdown
document.addEventListener('DOMContentLoaded', loadLoginAccounts);