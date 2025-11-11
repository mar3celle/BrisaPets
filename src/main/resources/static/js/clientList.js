document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('client-search');
    const table = document.getElementById('clients-table');
    const rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');
    
    // Search functionality
    searchInput.addEventListener('keyup', function() {
        const searchTerm = this.value.toLowerCase();
        
        for (let i = 0; i < rows.length; i++) {
            const row = rows[i];
            const nameCell = row.cells[0];
            const emailCell = row.cells[1];
            
            if (nameCell && emailCell) {
                const name = nameCell.textContent.toLowerCase();
                const email = emailCell.textContent.toLowerCase();
                
                if (name.includes(searchTerm) || email.includes(searchTerm)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            }
        }
    });
    
    // Export functionality
    document.getElementById('export-btn').addEventListener('click', function() {
        exportToCSV();
    });
    
    function exportToCSV() {
        const table = document.getElementById('clients-table');
        const rows = table.querySelectorAll('tr');
        let csvContent = '';
        
        rows.forEach(row => {
            const cells = row.querySelectorAll('th, td');
            const rowData = Array.from(cells).map(cell => cell.textContent.trim()).join(',');
            csvContent += rowData + '\n';
        });
        
        const blob = new Blob([csvContent], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'clientes.csv';
        a.click();
        window.URL.revokeObjectURL(url);
    }
});