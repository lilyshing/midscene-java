// Midscene Playground 前端应用

// 全局变量
let sessionId = null;
let stompClient = null;
let connected = false;

// 初始化应用
$(document).ready(function() {
    // 连接按钮点击事件
    $('#connectBtn').click(connectToPlatform);
    
    // 执行按钮点击事件
    $('#executeBtn').click(executeCommand);
    
    // 断开连接按钮点击事件
    $('#disconnectBtn').click(disconnect);
    
    // 清除输出按钮点击事件
    $('#clearBtn').click(clearOutput);
    
    // 命令类型改变事件
    $('#commandType').change(updateParametersPlaceholder);
    
    // 平台类型改变事件 - 设置默认服务器URL
    $('#platformType').change(function() {
        if ($(this).val() === 'webdriver') {
            $('#serverUrl').val('http://localhost:9515/');
        } else if ($(this).val() === 'android') {
            $('#serverUrl').val('http://localhost:4723/');
        } else if ($(this).val() === 'ios') {
            $('#serverUrl').val('http://localhost:4723/');
        }
    });
    
    // 初始化参数占位符
    updateParametersPlaceholder();
    
    // 设置初始服务器URL
    $('#serverUrl').val('http://localhost:9515/');
    
    // 显示系统信息
    appendOutput('Playground initialized. Ready to connect.');
});

// 更新参数占位符
function updateParametersPlaceholder() {
    const command = $('#commandType').val();
    let placeholder = '';
    
    switch(command) {
        case 'click':
            placeholder = '{"x": 100, "y": 200}';
            break;
        case 'input_text':
            placeholder = '{"text": "Hello World"}';
            break;
        case 'scroll':
            placeholder = '{"deltaX": 0, "deltaY": 100}';
            break;
        case 'navigate':
            placeholder = '{"url": "https://www.example.com"}';
            break;
        case 'take_screenshot':
            placeholder = '{"filename": "screenshot.png"}';
            break;
        default:
            placeholder = '{}';
    }
    
    $('#commandParams').attr('placeholder', placeholder);
}

// 连接到平台
function connectToPlatform() {
    const platformType = $('#platformType').val();
    const serverUrl = $('#serverUrl').val();
    
    if (!platformType || !serverUrl) {
        appendOutput('Error: Platform type and server URL are required', true);
        return;
    }
    
    appendOutput(`Connecting to ${platformType} platform at ${serverUrl}...`);
    
    // 禁用连接按钮
    $('#connectBtn').prop('disabled', true);
    
    $.ajax({
        url: '/playground/api/sessions',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({platformType: platformType, serverUrl: serverUrl}),
        success: function(response) {
            sessionId = response.sessionId;
            connected = true;
            
            $('#sessionInfo').val(JSON.stringify(response, null, 2));
            appendOutput(`Connected successfully! Session ID: ${sessionId}`);
            
            // 连接WebSocket
            connectWebSocket();
            
            // 更新UI状态
            $('#disconnectBtn').prop('disabled', false);
            $('#executeBtn').prop('disabled', false);
        },
        error: function(xhr, status, error) {
            appendOutput(`Connection failed: ${xhr.responseText || error}`, true);
            $('#connectBtn').prop('disabled', false);
        }
    });
}

// 连接WebSocket
function connectWebSocket() {
    const socket = new SockJS('/playground/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        appendOutput('WebSocket connected');
        
        // 订阅执行结果
        stompClient.subscribe('/topic/execution-results/' + sessionId, function(message) {
            const result = JSON.parse(message.body);
            displayExecutionResult(result);
        });
        
        // 订阅系统事件
        stompClient.subscribe('/topic/system-events', function(message) {
            const event = JSON.parse(message.body);
            appendOutput(`[System] ${event.message}`, false, false);
        });
    }, function(error) {
        appendOutput(`WebSocket connection failed: ${error}`, true);
    });
}

// 执行命令
function executeCommand() {
    if (!connected || !sessionId) {
        appendOutput('Error: Not connected to any platform', true);
        return;
    }
    
    const commandType = $('#commandType').val();
    let parameters;
    
    try {
        const paramsText = $('#commandParams').val().trim() || '{}';
        parameters = JSON.parse(paramsText);
    } catch (e) {
        appendOutput(`Error: Invalid JSON parameters - ${e.message}`, true);
        return;
    }
    
    appendOutput(`Executing command: ${commandType} with parameters: ${JSON.stringify(parameters)}`);
    
    $.ajax({
        url: '/playground/api/commands',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            sessionId: sessionId,
            commandType: commandType,
            parameters: parameters
        }),
        success: function(response) {
            if (response.success) {
                appendOutput('Command execution initiated');
            } else {
                appendOutput(`Failed to initiate command: ${response.message}`, true);
            }
        },
        error: function(xhr, status, error) {
            appendOutput(`Command execution failed: ${xhr.responseText || error}`, true);
        }
    });
}

// 显示执行结果
function displayExecutionResult(result) {
    if (result.success) {
        appendOutput(`Command executed successfully`, false);
        
        // 如果有结果数据，格式化并显示
        if (result.resultData) {
            try {
                const formattedData = JSON.stringify(result.resultData, null, 2);
                appendOutput(formattedData, false, true);
            } catch (e) {
                appendOutput(`Error formatting result data: ${e.message}`, true);
            }
        }
    } else {
        appendOutput(`Command failed: ${result.message}`, true);
        if (result.errorDetails) {
            appendOutput(`Error details: ${result.errorDetails}`, true);
        }
    }
}

// 断开连接
function disconnect() {
    if (!connected || !sessionId) {
        appendOutput('Not connected to any platform', true);
        return;
    }
    
    appendOutput('Disconnecting from platform...');
    
    // 先断开WebSocket连接
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
    }
    
    // 然后关闭服务器端会话
    $.ajax({
        url: `/playground/api/sessions/${sessionId}`,
        type: 'DELETE',
        success: function() {
            appendOutput('Disconnected successfully');
            resetState();
        },
        error: function(xhr, status, error) {
            appendOutput(`Disconnect failed: ${xhr.responseText || error}`, true);
            // 即使失败也重置本地状态
            resetState();
        }
    });
}

// 重置状态
function resetState() {
    sessionId = null;
    connected = false;
    
    $('#sessionInfo').val('');
    $('#connectBtn').prop('disabled', false);
    $('#executeBtn').prop('disabled', true);
    $('#disconnectBtn').prop('disabled', true);
}

// 在输出区域追加文本
function appendOutput(text, isError = false, isCode = false) {
    const outputDiv = $('#resultOutput');
    const timestamp = new Date().toLocaleTimeString();
    
    let line;
    if (isCode) {
        // 代码块，使用pre标签
        line = `<pre class="${isError ? 'text-danger' : 'text-success'}" style="margin: 5px 0; padding: 10px; background-color: #282c34; color: #abb2bf; border-radius: 4px;">${text}</pre>`;
    } else {
        // 普通文本行
        const className = isError ? 'text-danger' : 
                         text.includes('[System]') ? 'text-info' : 'text-dark';
        line = `<div><span class="text-muted small">[${timestamp}]</span> <span class="${className}">${text}</span></div>`;
    }
    
    outputDiv.append(line);
    outputDiv.scrollTop(outputDiv.prop('scrollHeight'));
}

// 清除输出
function clearOutput() {
    $('#resultOutput').empty();
    appendOutput('Output cleared');
}

// 键盘快捷键
$(document).keydown(function(e) {
    // Ctrl+Enter 执行命令
    if (e.ctrlKey && e.keyCode === 13) {
        executeCommand();
    }
    // Ctrl+L 清除输出
    if (e.ctrlKey && e.keyCode === 76) {
        e.preventDefault();
        clearOutput();
    }
});