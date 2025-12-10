// Google Maps Client-Side Implementation
let map;
let marker;
let geocoder;

// Initialize Google Maps
function initMap() {
    // Default location (Braga, Portugal)
    const defaultLocation = { lat: 41.5518, lng: -8.4229 };
    
    map = new google.maps.Map(document.getElementById('map'), {
        zoom: 13,
        center: defaultLocation,
        styles: [
            {
                featureType: 'poi',
                elementType: 'labels',
                stylers: [{ visibility: 'off' }]
            }
        ]
    });
    
    geocoder = new google.maps.Geocoder();
    
    marker = new google.maps.Marker({
        position: defaultLocation,
        map: map,
        draggable: true,
        title: 'Brisa Pets Location'
    });
    
    // Event listeners
    map.addListener('click', handleMapClick);
    marker.addListener('dragend', handleMarkerDrag);
    
    // Initialize with default location
    updateLocationInfo(defaultLocation.lat, defaultLocation.lng);
}

// Handle map click
function handleMapClick(event) {
    const lat = event.latLng.lat();
    const lng = event.latLng.lng();
    
    marker.setPosition(event.latLng);
    updateLocationInfo(lat, lng);
}

// Handle marker drag
function handleMarkerDrag(event) {
    const lat = event.latLng.lat();
    const lng = event.latLng.lng();
    
    updateLocationInfo(lat, lng);
}

// Update location information
function updateLocationInfo(lat, lng) {
    document.getElementById('currentCoords').textContent = 
        `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
    
    // Reverse geocode to get address
    reverseGeocode(lat, lng);
}

// Search address
function searchAddress() {
    const address = document.getElementById('addressInput').value;
    if (!address) return;
    
    geocoder.geocode({ address: address }, (results, status) => {
        if (status === 'OK') {
            const location = results[0].geometry.location;
            map.setCenter(location);
            marker.setPosition(location);
            
            updateLocationInfo(location.lat(), location.lng());
            document.getElementById('currentAddress').textContent = results[0].formatted_address;
        } else {
            alert('Endereço não encontrado: ' + status);
        }
    });
}

// Reverse geocode
function reverseGeocode(lat, lng) {
    const latlng = { lat: lat, lng: lng };
    
    geocoder.geocode({ location: latlng }, (results, status) => {
        if (status === 'OK') {
            if (results[0]) {
                document.getElementById('currentAddress').textContent = results[0].formatted_address;
            }
        } else {
            document.getElementById('currentAddress').textContent = 'Endereço não encontrado';
        }
    });
}

// Get current location
function getCurrentLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const lat = position.coords.latitude;
                const lng = position.coords.longitude;
                const location = { lat: lat, lng: lng };
                
                map.setCenter(location);
                marker.setPosition(location);
                updateLocationInfo(lat, lng);
            },
            (error) => {
                alert('Erro ao obter localização: ' + error.message);
            }
        );
    } else {
        alert('Geolocalização não é suportada neste navegador');
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Add event listeners
    const addressInput = document.getElementById('addressInput');
    if (addressInput) {
        addressInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchAddress();
            }
        });
    }
});