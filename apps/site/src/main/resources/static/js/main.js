// 全局JavaScript功能文件

// 等待DOM内容加载完成
document.addEventListener('DOMContentLoaded', function() {
    // 初始化FAQ手风琴功能
    initFaqAccordion();
    
    // 初始化导航栏滚动效果
    initNavbarScroll();
    
    // 初始化平滑滚动
    initSmoothScroll();
    
    // 初始化搜索功能
    initSearch();
    
    // 初始化代码高亮
    initCodeHighlight();
    
    // 初始化响应式菜单
    initResponsiveMenu();
});

/**
 * 初始化FAQ手风琴功能
 */
function initFaqAccordion() {
    const accordionHeaders = document.querySelectorAll('.accordion-header');
    if (accordionHeaders.length > 0) {
        accordionHeaders.forEach(header => {
            header.addEventListener('click', function() {
                const item = this.parentElement;
                
                // 关闭所有其他项
                document.querySelectorAll('.accordion-item').forEach(i => {
                    if (i !== item) {
                        i.classList.remove('active');
                    }
                });
                
                // 切换当前项
                item.classList.toggle('active');
                
                // 动画效果
                const content = this.nextElementSibling;
                if (content) {
                    if (item.classList.contains('active')) {
                        content.style.maxHeight = content.scrollHeight + 'px';
                    } else {
                        content.style.maxHeight = '0';
                    }
                }
            });
        });
    }
    
    // 类别筛选功能
    const categoryTabs = document.querySelectorAll('.category-tab');
    if (categoryTabs.length > 0) {
        categoryTabs.forEach(tab => {
            tab.addEventListener('click', function() {
                // 更新标签状态
                categoryTabs.forEach(t => t.classList.remove('active'));
                this.classList.add('active');
                
                // 筛选内容
                const category = this.getAttribute('data-category');
                const items = document.querySelectorAll('.accordion-item');
                
                items.forEach(item => {
                    if (category === 'all' || item.getAttribute('data-category') === category) {
                        item.style.display = 'block';
                        // 添加淡入动画
                        setTimeout(() => {
                            item.style.opacity = '1';
                            item.style.transform = 'translateY(0)';
                        }, 10);
                    } else {
                        item.style.opacity = '0';
                        item.style.transform = 'translateY(10px)';
                        setTimeout(() => {
                            item.style.display = 'none';
                            // 关闭已隐藏的项
                            item.classList.remove('active');
                            const content = item.querySelector('.accordion-content');
                            if (content) content.style.maxHeight = '0';
                        }, 300);
                    }
                });
            });
        });
    }
}

/**
 * 初始化导航栏滚动效果
 */
function initNavbarScroll() {
    const header = document.querySelector('header');
    if (header) {
        let lastScrollTop = 0;
        
        window.addEventListener('scroll', function() {
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            
            // 向下滚动且滚动距离大于100px时，添加阴影
            if (scrollTop > 100) {
                header.classList.add('navbar-scrolled');
            } else {
                header.classList.remove('navbar-scrolled');
            }
            
            // 向上滚动时显示导航栏，向下滚动隐藏（可选项）
            if (scrollTop > lastScrollTop && scrollTop > 300) {
                header.classList.add('navbar-hidden');
            } else {
                header.classList.remove('navbar-hidden');
            }
            
            lastScrollTop = scrollTop <= 0 ? 0 : scrollTop;
        });
        
        // 添加滚动相关的CSS
        const style = document.createElement('style');
        style.textContent = `
            header {
                transition: all 0.3s ease;
            }
            header.navbar-scrolled {
                box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                background-color: rgba(255, 255, 255, 0.98);
                backdrop-filter: blur(10px);
            }
            header.navbar-hidden {
                transform: translateY(-100%);
            }
        `;
        document.head.appendChild(style);
    }
}

/**
 * 初始化平滑滚动
 */
function initSmoothScroll() {
    // 为所有内部链接添加平滑滚动
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;
            
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                const headerHeight = document.querySelector('header')?.offsetHeight || 0;
                const targetPosition = targetElement.getBoundingClientRect().top + window.pageYOffset - headerHeight;
                
                window.scrollTo({
                    top: targetPosition,
                    behavior: 'smooth'
                });
                
                // 移动端点击链接后关闭菜单
                const mobileMenu = document.querySelector('.mobile-menu');
                if (mobileMenu && mobileMenu.classList.contains('open')) {
                    mobileMenu.classList.remove('open');
                    document.querySelector('.menu-toggle')?.classList.remove('active');
                }
            }
        });
    });
}

/**
 * 初始化搜索功能
 */
function initSearch() {
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const searchInput = this.querySelector('input[type="text"]');
            if (searchInput && searchInput.value.trim()) {
                const searchTerm = searchInput.value.trim();
                console.log('搜索关键词:', searchTerm);
                
                // 这里可以实现实际的搜索逻辑
                // 例如筛选FAQ内容或跳转到搜索结果页面
                performSearch(searchTerm);
                
                // 搜索动画效果
                searchInput.classList.add('searching');
                setTimeout(() => {
                    searchInput.classList.remove('searching');
                }, 1000);
            }
        });
    }
}

/**
 * 执行搜索
 */
function performSearch(term) {
    const accordionItems = document.querySelectorAll('.accordion-item');
    const noResults = document.getElementById('no-results');
    let hasResults = false;
    
    accordionItems.forEach(item => {
        const question = item.querySelector('.accordion-question')?.textContent.toLowerCase() || '';
        const answer = item.querySelector('.accordion-answer')?.textContent.toLowerCase() || '';
        const searchTerm = term.toLowerCase();
        
        if (question.includes(searchTerm) || answer.includes(searchTerm)) {
            item.style.display = 'block';
            item.style.opacity = '1';
            item.style.transform = 'translateY(0)';
            hasResults = true;
            
            // 高亮匹配的文本
            highlightText(item, searchTerm);
        } else {
            item.style.opacity = '0';
            item.style.transform = 'translateY(10px)';
            setTimeout(() => {
                item.style.display = 'none';
            }, 300);
        }
    });
    
    // 显示/隐藏无结果提示
    if (noResults) {
        noResults.style.display = hasResults ? 'none' : 'block';
    }
}

/**
 * 高亮文本
 */
function highlightText(element, term) {
    if (!term) return;
    
    const textElements = element.querySelectorAll('.accordion-question, .accordion-answer');
    textElements.forEach(el => {
        const originalHTML = el.getAttribute('data-original-html') || el.innerHTML;
        el.setAttribute('data-original-html', originalHTML);
        
        const regex = new RegExp(`(${term})`, 'gi');
        const highlightedHTML = originalHTML.replace(regex, '<mark>$1</mark>');
        el.innerHTML = highlightedHTML;
    });
}

/**
 * 初始化代码高亮（简易版）
 */
function initCodeHighlight() {
    const codeBlocks = document.querySelectorAll('pre code');
    if (codeBlocks.length > 0) {
        // 添加代码高亮的基础样式
        const style = document.createElement('style');
        style.textContent = `
            pre {
                background-color: #f6f8fa;
                border-radius: 6px;
                padding: 16px;
                overflow-x: auto;
                margin-bottom: 1rem;
            }
            
            code {
                font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                font-size: 14px;
                line-height: 1.5;
                color: #24292e;
            }
            
            .keyword {
                color: #d73a49;
            }
            
            .string {
                color: #032f62;
            }
            
            .comment {
                color: #6a737d;
                font-style: italic;
            }
            
            .number {
                color: #005cc5;
            }
            
            .function {
                color: #6f42c1;
            }
        `;
        document.head.appendChild(style);
        
        // 简单的语法高亮实现
        codeBlocks.forEach(block => {
            let code = block.textContent;
            
            // 高亮关键词
            code = code.replace(/\b(function|const|let|var|if|else|for|while|return|true|false|null|undefined)\b/g, '<span class="keyword">$1</span>');
            
            // 高亮字符串
            code = code.replace(/('.*?'|".*?")/g, '<span class="string">$1</span>');
            
            // 高亮注释
            code = code.replace(/\/\/.*$/gm, '<span class="comment">$&</span>');
            code = code.replace(/\/\*[\s\S]*?\*\//g, '<span class="comment">$&</span>');
            
            // 高亮数字
            code = code.replace(/\b\d+(\.\d+)?\b/g, '<span class="number">$&</span>');
            
            // 高亮函数名
            code = code.replace(/\b(\w+)\s*\(/g, '<span class="function">$1</span>(');
            
            block.innerHTML = code;
        });
    }
}

/**
 * 初始化响应式菜单
 */
function initResponsiveMenu() {
    // 检查是否有移动菜单
    const menuToggle = document.querySelector('.menu-toggle');
    const mobileMenu = document.querySelector('.mobile-menu');
    
    if (menuToggle && mobileMenu) {
        menuToggle.addEventListener('click', function() {
            this.classList.toggle('active');
            mobileMenu.classList.toggle('open');
            
            // 阻止页面滚动
            document.body.style.overflow = mobileMenu.classList.contains('open') ? 'hidden' : '';
        });
        
        // 添加移动菜单样式
        const style = document.createElement('style');
        style.textContent = `
            @media (max-width: 768px) {
                .menu-toggle {
                    display: block;
                    background: none;
                    border: none;
                    font-size: 24px;
                    cursor: pointer;
                    color: var(--dark-text);
                }
                
                .mobile-menu {
                    position: fixed;
                    top: 70px;
                    left: 0;
                    width: 100%;
                    background: white;
                    transform: translateY(-150%);
                    transition: transform 0.3s ease;
                    z-index: 999;
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                    padding: 20px;
                }
                
                .mobile-menu.open {
                    transform: translateY(0);
                }
                
                .mobile-menu .nav {
                    display: flex;
                    flex-direction: column;
                    gap: 15px;
                }
                
                .mobile-menu .nav-link {
                    font-size: 18px;
                    padding: 10px 0;
                    border-bottom: 1px solid var(--border-color);
                }
            }
            
            @media (min-width: 769px) {
                .menu-toggle {
                    display: none;
                }
                
                .mobile-menu {
                    display: none;
                }
            }
        `;
        document.head.appendChild(style);
        
        // 为现有的导航栏创建移动菜单
        const nav = document.querySelector('.nav');
        if (nav) {
            const mobileNav = nav.cloneNode(true);
            mobileMenu.innerHTML = '';
            mobileMenu.appendChild(mobileNav);
        }
    }
}

/**
 * 加载动态内容
 */
function loadDynamicContent(url, targetElementId) {
    const targetElement = document.getElementById(targetElementId);
    if (!targetElement) return;
    
    // 显示加载状态
    targetElement.innerHTML = '<div class="loading">加载中...</div>';
    
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(html => {
            targetElement.innerHTML = html;
            // 重新初始化新内容中的交互功能
            initFaqAccordion();
            initSmoothScroll();
            initCodeHighlight();
        })
        .catch(error => {
            targetElement.innerHTML = '<div class="error">加载失败，请稍后重试</div>';
            console.error('加载内容失败:', error);
        });
}

/**
 * 表单验证
 */
function validateForm(form) {
    const inputs = form.querySelectorAll('[required]');
    let isValid = true;
    
    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.classList.add('error');
            isValid = false;
        } else {
            input.classList.remove('error');
        }
    });
    
    // 验证电子邮件
    const emailInputs = form.querySelectorAll('input[type="email"]');
    emailInputs.forEach(input => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (input.value && !emailRegex.test(input.value)) {
            input.classList.add('error');
            isValid = false;
        }
    });
    
    return isValid;
}

/**
 * 设置Cookie
 */
function setCookie(name, value, days) {
    const expires = new Date();
    expires.setTime(expires.getTime() + (days * 24 * 60 * 60 * 1000));
    document.cookie = name + '=' + encodeURIComponent(value) + ';expires=' + expires.toUTCString() + ';path=/';
}

/**
 * 获取Cookie
 */
function getCookie(name) {
    const cookieName = name + '=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const cookieArray = decodedCookie.split(';');
    
    for (let i = 0; i < cookieArray.length; i++) {
        let cookie = cookieArray[i];
        while (cookie.charAt(0) === ' ') {
            cookie = cookie.substring(1);
        }
        if (cookie.indexOf(cookieName) === 0) {
            return cookie.substring(cookieName.length, cookie.length);
        }
    }
    return '';
}

/**
 * 检测是否为移动设备
 */
function isMobileDevice() {
    return (typeof window.orientation !== 'undefined') || (navigator.userAgent.indexOf('IEMobile') !== -1);
}

/**
 * 显示通知
 */
function showNotification(message, type = 'info', duration = 3000) {
    // 创建通知元素
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    // 添加到页面
    document.body.appendChild(notification);
    
    // 添加样式
    const style = document.createElement('style');
    style.textContent = `
        .notification {
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 16px 24px;
            border-radius: 6px;
            color: white;
            font-weight: 500;
            z-index: 9999;
            transform: translateX(100%);
            transition: transform 0.3s ease;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
        
        .notification.show {
            transform: translateX(0);
        }
        
        .notification-info {
            background-color: var(--info-color);
        }
        
        .notification-success {
            background-color: var(--success-color);
        }
        
        .notification-warning {
            background-color: var(--warning-color);
            color: var(--dark-text);
        }
        
        .notification-error {
            background-color: var(--danger-color);
        }
    `;
    document.head.appendChild(style);
    
    // 显示通知
    setTimeout(() => {
        notification.classList.add('show');
    }, 10);
    
    // 自动关闭
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, duration);
}