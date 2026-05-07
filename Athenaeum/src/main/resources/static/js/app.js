// Athenaeum JavaScript Utilities

document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Initialize popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });

    // Auto-hide alerts
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            if (alert.classList.contains('alert-success') || alert.classList.contains('alert-info')) {
                var bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        });
    }, 5000);

    // File upload drag and drop
    var fileInputs = document.querySelectorAll('input[type="file"]');
    fileInputs.forEach(function(fileInput) {
        var dropArea = fileInput.closest('.card-body');
        if (dropArea) {
            ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
                dropArea.addEventListener(eventName, preventDefaults, false);
            });

            ['dragenter', 'dragover'].forEach(eventName => {
                dropArea.addEventListener(eventName, highlight, false);
            });

            ['dragleave', 'drop'].forEach(eventName => {
                dropArea.addEventListener(eventName, unhighlight, false);
            });

            dropArea.addEventListener('drop', handleDrop, false);

            function preventDefaults(e) {
                e.preventDefault();
                e.stopPropagation();
            }

            function highlight(e) {
                dropArea.classList.add('dragover');
            }

            function unhighlight(e) {
                dropArea.classList.remove('dragover');
            }

            function handleDrop(e) {
                var dt = e.dataTransfer;
                var files = dt.files;
                if (files.length > 0) {
                    fileInput.files = files;
                    updateFileInputDisplay(fileInput, files[0]);
                }
            }
        }
    });

    // Update file input display
    function updateFileInputDisplay(input, file) {
        var label = input.nextElementSibling;
        if (label && label.classList.contains('form-label')) {
            label.textContent = 'Selected: ' + file.name;
            label.classList.add('text-success');
        }
    }

    // Search functionality
    var searchForms = document.querySelectorAll('form[action*="search"]');
    searchForms.forEach(function(form) {
        form.addEventListener('submit', function(e) {
            var query = form.querySelector('input[name="q"]');
            if (query && query.value.trim() === '') {
                e.preventDefault();
                showAlert('Please enter a search query', 'warning');
                query.focus();
            }
        });
    });

    // Auto-resize textareas
    var textareas = document.querySelectorAll('textarea');
    textareas.forEach(function(textarea) {
        textarea.addEventListener('input', function() {
            this.style.height = 'auto';
            this.style.height = (this.scrollHeight) + 'px';
        });
    });

    // Confirm delete actions
    var deleteButtons = document.querySelectorAll('[data-confirm-delete]');
    deleteButtons.forEach(function(button) {
        button.addEventListener('click', function(e) {
            if (!confirm('Are you sure you want to delete this item? This action cannot be undone.')) {
                e.preventDefault();
            }
        });
    });
});

// Utility functions
function showAlert(message, type = 'info') {
    var alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    var container = document.querySelector('.container');
    if (container) {
        container.insertBefore(alertDiv, container.firstChild);

        // Auto-hide after 5 seconds
        setTimeout(function() {
            var bsAlert = new bootstrap.Alert(alertDiv);
            bsAlert.close();
        }, 5000);
    }
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(function() {
        showAlert('Copied to clipboard!', 'success');
    }).catch(function(err) {
        showAlert('Failed to copy to clipboard', 'danger');
    });
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function formatDate(dateString) {
    const options = { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return new Date(dateString).toLocaleDateString('en-US', options);
}

// Real-time search (if needed)
function setupLiveSearch(inputSelector, resultsSelector) {
    var searchInput = document.querySelector(inputSelector);
    var resultsContainer = document.querySelector(resultsSelector);

    if (!searchInput || !resultsContainer) return;

    var debounceTimer;

    searchInput.addEventListener('input', function() {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(function() {
            var query = searchInput.value.trim();
            if (query.length >= 3) {
                performLiveSearch(query, resultsContainer);
            } else {
                resultsContainer.innerHTML = '';
            }
        }, 300);
    });
}

function performLiveSearch(query, container) {
    // This would make an AJAX call to the search API
    fetch(`/search/api?q=${encodeURIComponent(query)}`)
        .then(response => response.json())
        .then(data => {
            displaySearchResults(data, container);
        })
        .catch(error => {
            console.error('Search error:', error);
            container.innerHTML = '<div class="alert alert-danger">Search error occurred</div>';
        });
}

function displaySearchResults(results, container) {
    if (results.length === 0) {
        container.innerHTML = '<div class="text-muted">No results found</div>';
        return;
    }

    var html = '<div class="list-group">';
    results.forEach(function(result) {
        html += `
            <a href="/${result.type}s/${result.id}" class="list-group-item list-group-item-action">
                <div class="d-flex w-100 justify-content-between">
                    <h6 class="mb-1">${result.title}</h6>
                    <small class="badge bg-${result.type === 'document' ? 'primary' : 'success'}">${result.type}</small>
                </div>
                <p class="mb-1 text-muted">${result.content.substring(0, 100)}...</p>
            </a>
        `;
    });
    html += '</div>';

    container.innerHTML = html;
}