// Front : consomme notre propre service web REST (/api/...)
const API = "/api/destinations";

const $ = (id) => document.getElementById(id);

// ---------- Utilitaires ----------
function toast(msg, isError = false) {
    const t = $("toast");
    t.textContent = msg;
    t.className = "show" + (isError ? " error" : "");
    setTimeout(() => (t.className = ""), 2800);
}

async function request(url, options = {}) {
    const res = await fetch(url, {
        headers: { "Content-Type": "application/json" },
        ...options,
    });
    if (res.status === 204) return null;
    const data = await res.json();
    if (!res.ok) {
        const details = data.errors ? " : " + Object.values(data.errors).join(", ") : "";
        throw new Error((data.message || "Erreur") + details);
    }
    return data;
}

function escapeHtml(s) {
    return String(s ?? "").replace(/[&<>"']/g, (c) =>
        ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

// ---------- Liste ----------
async function loadDestinations() {
    const search = $("search").value.trim();
    const status = $("filter").value;
    const params = new URLSearchParams();
    if (search) params.append("search", search);
    else if (status) params.append("status", status);

    try {
        const list = await request(`${API}?${params}`);
        render(list);
        loadStats();
    } catch (e) {
        toast(e.message, true);
    }
}

function render(list) {
    const container = $("list");
    if (!list.length) {
        container.innerHTML = `<div class="empty">Aucune destination trouvée 🧳</div>`;
        return;
    }
    container.innerHTML = list.map((d) => `
        <article class="card">
            <div class="card-head">
                <div>
                    <h3>${escapeHtml(d.city)}</h3>
                    <span class="country">📍 ${escapeHtml(d.country)}</span>
                </div>
                <span class="badge ${d.status}">${d.status === "VISITED" ? "Visitée" : "Planifiée"}</span>
            </div>
            <p>${escapeHtml(d.description) || "<i>Pas de description</i>"}</p>
            <div class="meta">
                <span>💰 ${d.budget ?? 0} DT</span>
                <span>📅 ${d.plannedDate ?? "—"}</span>
            </div>
            <div class="actions">
                <button class="btn small" onclick="showWeather(${d.id})">🌤️ Météo</button>
                <button class="btn small" onclick="editDestination(${d.id})">✏️ Modifier</button>
                <button class="btn small danger" onclick="deleteDestination(${d.id})">🗑️</button>
            </div>
        </article>`).join("");
}

async function loadStats() {
    const s = await request(`${API}/stats`);
    $("stat-total").textContent = s.total;
    $("stat-planned").textContent = s.planned;
    $("stat-visited").textContent = s.visited;
    $("stat-budget").textContent = `${s.totalBudget.toLocaleString("fr-FR")} DT`;
}

// ---------- Formulaire ----------
function openForm(d = null) {
    $("modal-title").textContent = d ? "Modifier la destination" : "Nouvelle destination";
    $("dest-id").value = d?.id ?? "";
    $("city").value = d?.city ?? "";
    $("country").value = d?.country ?? "";
    $("description").value = d?.description ?? "";
    $("budget").value = d?.budget ?? "";
    $("plannedDate").value = d?.plannedDate ?? "";
    $("status").value = d?.status ?? "PLANNED";
    $("modal").classList.add("open");
}

async function editDestination(id) {
    try {
        openForm(await request(`${API}/${id}`));
    } catch (e) {
        toast(e.message, true);
    }
}

async function deleteDestination(id) {
    if (!confirm("Supprimer cette destination ?")) return;
    try {
        await request(`${API}/${id}`, { method: "DELETE" });
        toast("Destination supprimée");
        loadDestinations();
    } catch (e) {
        toast(e.message, true);
    }
}

$("dest-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const id = $("dest-id").value;
    const body = {
        city: $("city").value.trim(),
        country: $("country").value.trim(),
        description: $("description").value.trim(),
        budget: $("budget").value ? Number($("budget").value) : null,
        plannedDate: $("plannedDate").value || null,
        status: $("status").value,
    };
    try {
        await request(id ? `${API}/${id}` : API, {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(body),
        });
        $("modal").classList.remove("open");
        toast(id ? "Destination modifiée ✔" : "Destination ajoutée ✔");
        loadDestinations();
    } catch (err) {
        toast(err.message, true);
    }
});

// ---------- Météo (API externe via notre backend) ----------
function weatherHtml(w) {
    return `
        <h2>Météo à ${escapeHtml(w.city)}</h2>
        <div class="weather-box">
            <div class="weather-now">
                <span class="icon">${w.currentIcon}</span>
                <div>
                    <div class="temp">${Math.round(w.currentTemperature)}°C</div>
                    <div>${w.currentDescription} · 💨 ${w.windSpeed} km/h</div>
                </div>
            </div>
            <div class="forecast">
                ${w.forecast.map((d) => `
                    <div class="day">
                        <b>${new Date(d.date).toLocaleDateString("fr-FR", { weekday: "short", day: "numeric" })}</b>
                        <span class="icon">${d.icon}</span>
                        ${Math.round(d.min)}° / ${Math.round(d.max)}°
                    </div>`).join("")}
            </div>
        </div>`;
}

async function showWeather(id) {
    $("weather-modal-content").innerHTML = "<p>Chargement...</p>";
    $("weather-modal").classList.add("open");
    try {
        $("weather-modal-content").innerHTML = weatherHtml(await request(`${API}/${id}/weather`));
    } catch (e) {
        $("weather-modal-content").innerHTML = `<p>❌ ${escapeHtml(e.message)}</p>`;
    }
}

$("weather-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const city = $("weather-city").value.trim();
    $("weather-result").innerHTML = "<p>Chargement...</p>";
    try {
        const w = await request(`/api/weather?city=${encodeURIComponent(city)}`);
        $("weather-result").innerHTML = weatherHtml(w);
    } catch (err) {
        $("weather-result").innerHTML = `<p>❌ ${escapeHtml(err.message)}</p>`;
    }
});

// ---------- Événements ----------
let timer;
$("search").addEventListener("input", () => {
    clearTimeout(timer);
    timer = setTimeout(loadDestinations, 300);
});
$("filter").addEventListener("change", loadDestinations);
$("btn-add").addEventListener("click", () => openForm());
$("btn-cancel").addEventListener("click", () => $("modal").classList.remove("open"));
$("btn-close-weather").addEventListener("click", () => $("weather-modal").classList.remove("open"));

loadDestinations();
