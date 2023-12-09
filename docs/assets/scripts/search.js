const header = document.getElementsByTagName('header')[0];
const wrapper = document.getElementsByClassName('wrapper')[0];
const paragraphs = Array.from(document.querySelectorAll('h1, h2, h3, h4')).map(p => p.innerText.toLowerCase());
const searchContainer = document.getElementById('search-container');
const searchInput = document.getElementById('search-input');
const resultsContainer = document.getElementById('results-container');
const noResult = document.createElement('li');
const searchContainerTop = searchContainer.getBoundingClientRect().top - 5;
const section = document.querySelectorAll('section')[0];
const topButton = document.getElementById("topButton");

noResult.innerText = 'No results'
noResult.setAttribute('id', 'no-results');

searchInput.setAttribute('onfocus', 'highlight()');
searchInput.setAttribute('onclick', 'showResults()');
searchInput.setAttribute('onkeyup', 'search()');

document.onload = addAnchor();
document.addEventListener('click', function(event) {
    if (!searchContainer.contains(event.target)) {
        hideResults();
    }
});
document.addEventListener('keydown', evt => {
    if (evt.key === 'Escape') {
        searchInput.blur();
        hideResults();
    }
});

window.onscroll = function() { stickyHeader() };
window.onresize = function() { stickyHeader() };

function highlight() {
    searchInput.classList.add('shadow');
}

function stickyHeader() {
    if (window.pageYOffset > searchContainerTop) {
        header.classList.add('sticky');
        header.style.width = wrapper.offsetWidth - 40 + 'px';
        section.classList.add('sticky-section');
        topButton.style.visibility = 'visible';
        topButton.style.opacity = '1';
    } else {
        header.classList.remove('sticky');
        header.style.width = 'auto';
        section.classList.remove('sticky-section');
        topButton.style.visibility = 'hidden';
        topButton.style.opacity = '0';
    }
}

function showResults() {
    if (resultsContainer.innerHTML != '') {
        resultsContainer.style.display = 'block';
    }
}

function hideResults() {
    resultsContainer.style.display = 'none';
    searchInput.classList.remove('shadow');
}

function addAnchor() {
    location.href = '#spectrum';
}

function navigateTo(anchor) {
    hideResults();

    location.href = '#' + anchor;
    window.scrollBy(0, -75);
}

function search() {
    const value = searchInput.value.toLowerCase();
    const results = paragraphs.filter(p => p.includes(value));

    resultsContainer.innerHTML = '';
    if (value == '') {
        hideResults();
        return;
    }

    if (!results.length) {
        resultsContainer.appendChild(noResult);
        return;
    }

    results
        .map(r => {
            const li = document.createElement('li');

            li.setAttribute('onclick', 'navigateTo("' + r.replaceAll(' ', '-') + '")');
            li.classList.add('search-result');
            li.innerText = r;

            return li;
        })
        .forEach(li => resultsContainer.appendChild(li));

    showResults();
}
