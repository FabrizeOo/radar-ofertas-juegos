// URL del backend en Render (puedes usar ?api=http://localhost:8081 para pruebas locales)
const API_BASE_URL = new URLSearchParams(window.location.search).get("api") || "https://radar-ofertas-juegos.onrender.com";

let paginaActual = 0;
const TAMANO_PAGINA = 24;
let tiendaSeleccionada = "TODAS";
let dealSeleccionado = "GENERAL";
let ofertasActuales = [];
let eventSource = null;
let debounceTimer = null;

const el = {
    liveIndicator: document.getElementById("liveIndicator"),
    liveText: document.getElementById("liveText"),
    btnRefreshCatalog: document.getElementById("btnRefreshCatalog"),
    heroSpotlight: document.getElementById("heroSpotlight"),
    statOfertas: document.getElementById("statOfertas"),
    statAhorro: document.getElementById("statAhorro"),
    statComentarios: document.getElementById("statComentarios"),
    statTiendas: document.getElementById("statTiendas"),
    inputBuscar: document.getElementById("inputBuscar"),
    selectAhorro: document.getElementById("selectAhorro"),
    storePills: document.getElementById("storePills"),
    ofertasGrid: document.getElementById("ofertasGrid"),
    btnPrev: document.getElementById("btnPrev"),
    btnNext: document.getElementById("btnNext"),
    paginaLabel: document.getElementById("paginaLabel"),
    formComentario: document.getElementById("formComentarioGlobal"),
    targetLabel: document.getElementById("targetLabel"),
    btnLimpiarTarget: document.getElementById("btnLimpiarTarget"),
    inputAutor: document.getElementById("inputAutor"),
    inputContenido: document.getElementById("inputContenido"),
    selectCalificacion: document.getElementById("selectCalificacion"),
    feedComentarios: document.getElementById("feedComentarios"),
    modalDetalle: document.getElementById("modalDetalle"),
    btnCerrarDetalle: document.getElementById("btnCerrarDetalle"),
    detalleBody: document.getElementById("detalleBody"),
    btnClasificarLote: document.getElementById("btnClasificarLote"),
    btnPurgarOfertas: document.getElementById("btnPurgarOfertas"),
    btnGenerarReporteBi: document.getElementById("btnGenerarReporteBi"),
    dbOpsResultado: document.getElementById("dbOpsResultado")
};

function iniciar() {
    registrarEventosUI();
    cargarTodo();
    conectarTiempoReal();
}

function registrarEventosUI() {
    el.inputBuscar.addEventListener("input", () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            paginaActual = 0;
            cargarOfertas();
        }, 240);
    });

    el.selectAhorro.addEventListener("change", () => {
        paginaActual = 0;
        cargarOfertas();
    });

    el.storePills.addEventListener("click", (e) => {
        const btn = e.target.closest(".pill");
        if (!btn) return;
        el.storePills.querySelectorAll(".pill").forEach(p => p.classList.remove("active"));
        btn.classList.add("active");
        tiendaSeleccionada = btn.dataset.store;
        paginaActual = 0;
        cargarOfertas();
    });

    el.btnPrev.addEventListener("click", () => {
        if (paginaActual > 0) {
            paginaActual--;
            cargarOfertas();
        }
    });

    el.btnNext.addEventListener("click", () => {
        paginaActual++;
        cargarOfertas();
    });

    if (el.btnRefreshCatalog) el.btnRefreshCatalog.addEventListener("click", actualizarCatalogo);
    el.formComentario.addEventListener("submit", enviarComentario);
    el.btnLimpiarTarget.addEventListener("click", () => seleccionarObjetivo("GENERAL", "Comunidad General"));
    el.btnCerrarDetalle.addEventListener("click", () => el.modalDetalle.classList.add("hidden"));
    el.modalDetalle.addEventListener("click", (e) => {
        if (e.target === el.modalDetalle) el.modalDetalle.classList.add("hidden");
    });
    if (el.btnClasificarLote) el.btnClasificarLote.addEventListener("click", ejecutarClasificacionLote);
    if (el.btnPurgarOfertas) el.btnPurgarOfertas.addEventListener("click", ejecutarPurgaOfertas);
    if (el.btnGenerarReporteBi) el.btnGenerarReporteBi.addEventListener("click", ejecutarReporteBi);
}

async function cargarTodo() {
    await Promise.all([cargarEstadisticas(), cargarOfertas(), cargarComentarios()]);
}

async function cargarEstadisticas() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas/estadisticas`);
        if (!res.ok) throw new Error();
        const stats = await res.json();
        pintarEstadisticas(stats);
    } catch {
        el.statTiendas.innerHTML = `<span class="store-chip">Conectando con catálogo...</span>`;
    }
}

function pintarEstadisticas(stats) {
    el.statOfertas.textContent = (stats.totalOfertas ?? 0).toLocaleString("es-ES");
    el.statComentarios.textContent = (stats.totalComentarios ?? 0).toLocaleString("es-ES");
    el.statAhorro.textContent = `${stats.ahorroPromedio ?? 0}%`;

    const tiendas = stats.porTienda || [];
    if (tiendas.length === 0) {
        el.statTiendas.innerHTML = `<span class="store-chip">Sin tiendas aún</span>`;
        return;
    }
    el.statTiendas.innerHTML = tiendas.slice(0, 5)
        .map(t => `<span class="store-chip">${escaparHtml(t.tiendaNombre)} (${t.cantidadOfertas})</span>`)
        .join("");
}

async function cargarOfertas() {
    const params = new URLSearchParams({
        q: el.inputBuscar.value.trim(),
        tienda: tiendaSeleccionada,
        ahorroMin: el.selectAhorro.value,
        pagina: paginaActual,
        tamano: TAMANO_PAGINA
    });

    el.paginaLabel.textContent = `Página ${paginaActual + 1}`;
    el.btnPrev.disabled = paginaActual === 0;

    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas?${params.toString()}`);
        if (!res.ok) throw new Error();
        ofertasActuales = await res.json();
        pintarHero(ofertasActuales[0]);
        pintarOfertas(ofertasActuales);
        el.btnNext.disabled = ofertasActuales.length < TAMANO_PAGINA;
    } catch {
        el.ofertasGrid.innerHTML = `<div class="empty-state">No pudimos cargar las ofertas en este momento. Intenta actualizar en unos segundos.</div>`;
    }
}

function urlCompraDirecta(dealId) {
    const idFinal = String(dealId).includes("%") ? dealId : encodeURIComponent(dealId);
    return `https://www.cheapshark.com/redirect?dealID=${idFinal}`;
}

function pintarHero(top) {
    if (!top) return;
    el.heroSpotlight.classList.remove("skeleton-hero");
    el.heroSpotlight.innerHTML = `
        <div class="hero-backdrop" style="background-image:url('${escaparHtml(top.urlImagen || "")}')" aria-hidden="true"></div>
        <div class="hero-content">
            <span class="hero-tag">${logoTienda(top.storeId) || icono("flame")} Destacada en ${escaparHtml(top.tiendaNombre || "Tienda oficial")}</span>
            <h2>${escaparHtml(top.titulo)}</h2>
            <div class="hero-meta">
                ${top.metacriticScore > 0 ? `<span>${icono("target")} Crítica: ${top.metacriticScore}/100</span>` : ""}
                ${top.steamRatingPercent > 0 ? `<span>${icono("thumb")} Jugadores: ${top.steamRatingPercent}%</span>` : ""}
                <span>${icono("heart")} ${top.likes || 0} me gusta</span>
            </div>
            <div class="hero-prices">
                <span class="hero-discount">-${Math.round(top.porcentajeAhorro)}%</span>
                <span class="hero-price-now">$${Number(top.precioOferta).toFixed(2)}</span>
                <span class="hero-price-old">$${Number(top.precioNormal).toFixed(2)}</span>
            </div>
            <div class="hero-actions">
                <a class="btn btn-mint" href="${urlCompraDirecta(top.dealId)}" target="_blank" rel="noopener noreferrer">
                    ${icono("cart")} Comprar en ${escaparHtml(top.tiendaNombre || "tienda")}
                </a>
                <button class="btn btn-outline" onclick="abrirDetalle('${encodeURIComponent(top.dealId)}')">
                    Ver detalles
                </button>
            </div>
        </div>
        <div class="hero-visual">
            <img src="${escaparHtml(top.urlImagen || "")}" alt="${escaparHtml(top.titulo)}">
        </div>
    `;
}

function pintarOfertas(ofertas) {
    if (!ofertas || ofertas.length === 0) {
        el.ofertasGrid.innerHTML = `<div class="empty-state">No encontramos juegos con esos filtros. Prueba otra búsqueda.</div>`;
        return;
    }

    el.ofertasGrid.innerHTML = ofertas.map((o, idx) => `
        <article class="deal-card" data-deal="${escaparHtml(o.dealId)}" style="animation-delay: ${Math.min(idx * 25, 300)}ms">
            <div class="deal-thumb" onclick="abrirDetalle('${encodeURIComponent(o.dealId)}')">
                <img src="${escaparHtml(o.urlImagen || "")}" alt="${escaparHtml(o.titulo)}" loading="lazy">
                <span class="deal-discount-tag">-${Math.round(o.porcentajeAhorro)}%</span>
                <span class="deal-store-tag">${logoTienda(o.storeId)}${escaparHtml(o.tiendaNombre || "Tienda")}</span>
            </div>
            <div class="deal-body">
                <h3 class="deal-title" onclick="abrirDetalle('${encodeURIComponent(o.dealId)}')" title="${escaparHtml(o.titulo)}">
                    ${escaparHtml(o.titulo)}
                </h3>
                <div class="deal-scores">
                    ${o.metacriticScore > 0 ? `<span class="score-pill" title="Puntuación de la crítica">${icono("target")} ${o.metacriticScore}</span>` : ""}
                    ${o.steamRatingPercent > 0 ? `<span class="score-pill" title="Valoración de jugadores">${icono("thumb")} ${o.steamRatingPercent}%</span>` : ""}
                    ${o.precioAlerta > 0 ? `<span class="score-pill alert-pill" title="Precio alerta configurado">Alerta: $${Number(o.precioAlerta).toFixed(2)}</span>` : ""}
                </div>
                <div class="deal-price-row">
                    <span class="price-now">$${Number(o.precioOferta).toFixed(2)}</span>
                    <span class="price-old">$${Number(o.precioNormal).toFixed(2)}</span>
                </div>
                <div class="deal-actions">
                    <a class="btn btn-mint" href="${urlCompraDirecta(o.dealId)}" target="_blank" rel="noopener noreferrer" title="Ir a la tienda para comprar">
                        ${icono("cart")} Ir a tienda
                    </a>
                    <button type="button" class="btn btn-outline" onclick="darLike('${encodeURIComponent(o.dealId)}')" title="Me gusta" aria-label="Me gusta">
                        ${icono("heart")} <span class="like-count">${o.likes || 0}</span>
                    </button>
                    <button type="button" class="btn btn-outline ${o.destacada ? "is-starred" : ""}" onclick="alternarDestacada('${encodeURIComponent(o.dealId)}', ${!o.destacada})" title="Destacar en BD (PATCH)" aria-label="Destacar oferta">
                        ${icono("star")}
                    </button>
                    <button type="button" class="btn btn-outline" onclick="seleccionarObjetivoDesdeBoton('${encodeURIComponent(o.dealId)}', '${encodeURIComponent(o.titulo)}')" title="Comentar sobre este juego" aria-label="Comentar sobre este juego">
                        ${icono("message")}
                    </button>
                </div>
            </div>
        </article>
    `).join("");
}

window.abrirDetalle = async function (encodedDealId) {
    const dealId = decodeURIComponent(encodedDealId);
    const oferta = ofertasActuales.find(x => x.dealId === dealId);
    if (!oferta) return;

    const ahorroDinero = Math.max(0, oferta.precioNormal - oferta.precioOferta).toFixed(2);
    el.detalleBody.innerHTML = `
        <img class="detail-banner" src="${escaparHtml(oferta.urlImagen || "")}" alt="${escaparHtml(oferta.titulo)}">
        <h2 class="detail-title">${escaparHtml(oferta.titulo)}</h2>
        <p class="detail-store">${logoTienda(oferta.storeId)}Disponible en <strong>${escaparHtml(oferta.tiendaNombre)}</strong></p>

        <div class="detail-grid">
            <div class="detail-stat">
                <span>Precio hoy</span>
                <strong class="accent-mint">$${Number(oferta.precioOferta).toFixed(2)}</strong>
            </div>
            <div class="detail-stat">
                <span>Ahorras</span>
                <strong>$${ahorroDinero} (-${Math.round(oferta.porcentajeAhorro)}%)</strong>
            </div>
            <div class="detail-stat">
                <span>Valoración</span>
                <strong>${oferta.metacriticScore > 0 ? oferta.metacriticScore + "/100" : (oferta.steamRatingPercent > 0 ? oferta.steamRatingPercent + "%" : "N/D")}</strong>
            </div>
        </div>

        <div class="detail-actions">
            <a class="btn btn-mint" href="${urlCompraDirecta(oferta.dealId)}" target="_blank" rel="noopener noreferrer">
                ${icono("cart")} Comprar ahora en ${escaparHtml(oferta.tiendaNombre)} ${icono("external")}
            </a>
            <button type="button" class="btn btn-primary" onclick="seleccionarDesdeModal('${encodeURIComponent(oferta.dealId)}', '${encodeURIComponent(oferta.titulo)}')">
                ${icono("message")} Escribir opinión
            </button>
        </div>

        <form class="alert-config-box" onsubmit="guardarAlertaModal(event, '${encodeURIComponent(oferta.dealId)}')">
            <label for="inputPrecioAlerta">Precio objetivo de alerta (USD) en MongoDB:</label>
            <div class="alert-config-row">
                <input id="inputPrecioAlerta" type="number" step="0.01" min="0" placeholder="Ej. ${(oferta.precioOferta * 0.85).toFixed(2)}" value="${oferta.precioAlerta > 0 ? oferta.precioAlerta : ""}">
                <label class="check-inline">
                    <input id="checkDestacadaModal" type="checkbox" ${oferta.destacada ? "checked" : ""}>
                    <span>Destacar</span>
                </label>
                <button type="submit" class="btn btn-outline">Guardar (PATCH)</button>
            </div>
        </form>

        <div class="detail-comments" id="detalleComentarios">
            <p class="detail-note">Cargando opiniones de este juego...</p>
        </div>
    `;
    el.modalDetalle.classList.remove("hidden");

    try {
        const res = await fetch(`${API_BASE_URL}/api/comentarios/oferta?dealId=${encodeURIComponent(dealId)}`);
        const lista = await res.json();
        const cont = document.getElementById("detalleComentarios");
        if (!lista.length) {
            cont.innerHTML = `<p class="detail-note">Aún no hay opiniones sobre este juego. ¡Sé el primero!</p>`;
        } else {
            cont.innerHTML = `<h3>Opiniones de jugadores</h3>` + lista.map(renderComentarioHtml).join("");
        }
    } catch {
        // Silencioso
    }
};

window.seleccionarDesdeModal = function (encodedDealId, encodedTitulo) {
    el.modalDetalle.classList.add("hidden");
    window.seleccionarObjetivoDesdeBoton(encodedDealId, encodedTitulo);
};

async function actualizarCatalogo() {
    el.btnRefreshCatalog.disabled = true;
    const original = el.btnRefreshCatalog.innerHTML;
    el.btnRefreshCatalog.classList.add("is-loading");
    el.btnRefreshCatalog.innerHTML = `${icono("refresh")}<span>Buscando rebajas...</span>`;
    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas/sincronizar?paginas=10`, { method: "POST" });
        if (!res.ok) throw new Error();
        await cargarTodo();
    } catch {
        alert("No se pudo actualizar el catálogo en este momento.");
    } finally {
        el.btnRefreshCatalog.disabled = false;
        el.btnRefreshCatalog.classList.remove("is-loading");
        el.btnRefreshCatalog.innerHTML = original;
    }
}

window.darLike = async function (encodedDealId) {
    const dealId = decodeURIComponent(encodedDealId);
    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas/like?dealId=${encodeURIComponent(dealId)}`, { method: "POST" });
        if (res.ok) {
            const actualizada = await res.json();
            actualizarLikesEnTarjeta(actualizada);
        }
    } catch (e) {
        console.error(e);
    }
};

window.seleccionarObjetivoDesdeBoton = function (encodedDealId, encodedTitulo) {
    seleccionarObjetivo(decodeURIComponent(encodedDealId), decodeURIComponent(encodedTitulo));
    el.inputContenido.focus();
};

function seleccionarObjetivo(dealId, titulo) {
    dealSeleccionado = dealId;
    el.targetLabel.querySelector("strong").textContent = titulo;
    el.btnLimpiarTarget.classList.toggle("hidden", dealId === "GENERAL");
}

function actualizarLikesEnTarjeta(oferta) {
    const cards = document.querySelectorAll(".deal-card");
    for (const card of cards) {
        if (card.dataset.deal === oferta.dealId) {
            const span = card.querySelector(".like-count");
            if (span) span.textContent = oferta.likes;
        }
    }
}

async function cargarComentarios() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/comentarios?limite=30`);
        if (!res.ok) throw new Error();
        const lista = await res.json();
        pintarComentarios(lista);
    } catch {
        // Silencioso
    }
}

function pintarComentarios(lista) {
    if (!lista || lista.length === 0) {
        el.feedComentarios.innerHTML = `<div class="empty-state">Sé el primero en opinar sobre una oferta.</div>`;
        return;
    }
    el.feedComentarios.innerHTML = lista.map(renderComentarioHtml).join("");
}

function renderComentarioHtml(c) {
    const nota = Math.min(5, Math.max(0, c.calificacion || 5));
    const estrellas = Array.from({ length: 5 }, (_, i) => icono("star", i < nota ? "on" : "off")).join("");
    const fecha = c.fechaCreacion ? new Date(c.fechaCreacion).toLocaleString("es-ES") : "Recién publicado";
    const inicial = String(c.autor || "?").replace(/^@/, "").charAt(0) || "?";
    return `
        <div class="comment-card" data-comment-id="${escaparHtml(c.id || "")}">
            <div class="comment-head">
                <span class="comment-who">
                    <span class="comment-avatar" style="background:${colorDesdeTexto(c.autor)}">${escaparHtml(inicial)}</span>
                    <span class="comment-author">${escaparHtml(c.autor)}</span>
                </span>
                <span class="comment-stars" role="img" aria-label="${nota} de 5 estrellas">${estrellas}</span>
            </div>
            <div class="comment-game">${icono("gamepad")} ${escaparHtml(c.tituloJuego || "Comunidad General")}</div>
            <p class="comment-body">${escaparHtml(c.contenido)}</p>
            <div class="comment-foot">
                <span class="comment-time">${fecha}</span>
                ${c.id ? `
                <div class="comment-actions">
                    <button type="button" class="icon-action" onclick="editarComentarioUI('${escaparHtml(c.id)}', ${nota})" title="Editar opinión (PUT)" aria-label="Editar opinión">${icono("edit")}</button>
                    <button type="button" class="icon-action danger" onclick="eliminarComentarioUI('${escaparHtml(c.id)}')" title="Eliminar opinión (DELETE)" aria-label="Eliminar opinión">${icono("trash")}</button>
                </div>` : ""}
            </div>
        </div>
    `;
}

async function enviarComentario(e) {
    e.preventDefault();
    const payload = {
        dealId: dealSeleccionado,
        autor: el.inputAutor.value.trim(),
        contenido: el.inputContenido.value.trim(),
        calificacion: Number(el.selectCalificacion.value)
    };
    if (!payload.contenido) return;

    try {
        const res = await fetch(`${API_BASE_URL}/api/comentarios`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            el.inputContenido.value = "";
        }
    } catch {
        alert("No se pudo publicar tu comentario.");
    }
}

function conectarTiempoReal() {
    if (eventSource) eventSource.close();
    eventSource = new EventSource(`${API_BASE_URL}/api/ofertas/stream`);

    eventSource.addEventListener("conectado", () => {
        if (el.liveIndicator) el.liveIndicator.className = "pulse-badge online";
        if (el.liveText) el.liveText.textContent = "Actualizado al instante";
    });

    eventSource.addEventListener("comentario", (event) => {
        const nuevo = JSON.parse(event.data);
        const vacio = el.feedComentarios.querySelector(".empty-state");
        if (vacio) vacio.remove();
        el.feedComentarios.insertAdjacentHTML("afterbegin", renderComentarioHtml(nuevo));
        const actual = Number(el.statComentarios.textContent.replace(/\D/g, "")) || 0;
        el.statComentarios.textContent = (actual + 1).toLocaleString("es-ES");
    });

    eventSource.addEventListener("like", (event) => {
        actualizarLikesEnTarjeta(JSON.parse(event.data));
    });

    eventSource.addEventListener("oferta_actualizada", () => {
        cargarOfertas();
    });

    eventSource.addEventListener("comentario_actualizado", (event) => {
        const editado = JSON.parse(event.data);
        const card = el.feedComentarios.querySelector(`[data-comment-id="${CSS.escape(editado.id)}"]`);
        if (card) card.outerHTML = renderComentarioHtml(editado);
    });

    eventSource.addEventListener("comentario_eliminado", (event) => {
        const { id } = JSON.parse(event.data);
        const card = el.feedComentarios.querySelector(`[data-comment-id="${CSS.escape(id)}"]`);
        if (card) card.remove();
        cargarEstadisticas();
    });

    eventSource.addEventListener("sincronizacion", (event) => {
        const data = JSON.parse(event.data);
        if (data.estadisticas) pintarEstadisticas(data.estadisticas);
        cargarOfertas();
    });

    eventSource.onerror = () => {
        if (el.liveIndicator) el.liveIndicator.className = "pulse-badge";
        if (el.liveText) el.liveText.textContent = "Reconectando...";
    };
}

function icono(nombre, clase = "") {
    return `<svg class="i ${clase}" aria-hidden="true"><use href="#i-${nombre}"/></svg>`;
}

// CheapShark publica el ícono de cada tienda como (storeID - 1).png
function logoTienda(storeId) {
    const n = parseInt(storeId, 10);
    if (!Number.isFinite(n) || n < 1) return "";
    return `<img class="store-logo" src="https://www.cheapshark.com/img/stores/icons/${n - 1}.png" alt="" width="14" height="14" loading="lazy" onerror="this.remove()">`;
}

function colorDesdeTexto(texto) {
    const paleta = ["#a78bfa", "#34d399", "#fbbf24", "#60a5fa", "#fb7185", "#22d3ee"];
    let h = 0;
    for (const ch of String(texto || "")) h = (h * 31 + ch.charCodeAt(0)) >>> 0;
    return paleta[h % paleta.length];
}

function escaparHtml(texto) {
    if (texto === null || texto === undefined) return "";
    return String(texto)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

window.alternarDestacada = async function (encodedDealId, destacada) {
    const dealId = decodeURIComponent(encodedDealId);
    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas/destacar?dealId=${encodeURIComponent(dealId)}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ destacada })
        });
        if (res.ok) cargarOfertas();
    } catch (e) {
        console.error(e);
    }
};

window.guardarAlertaModal = async function (e, encodedDealId) {
    e.preventDefault();
    const dealId = decodeURIComponent(encodedDealId);
    const val = document.getElementById("inputPrecioAlerta").value;
    const destacada = document.getElementById("checkDestacadaModal").checked;
    const precioAlerta = val === "" ? 0 : Number(val);
    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas/destacar?dealId=${encodeURIComponent(dealId)}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ precioAlerta, destacada })
        });
        if (res.ok) {
            el.modalDetalle.classList.add("hidden");
            cargarOfertas();
        }
    } catch {
        alert("No se pudo guardar la alerta en la base de datos.");
    }
};

window.editarComentarioUI = async function (id, notaActual) {
    const card = el.feedComentarios.querySelector(`[data-comment-id="${CSS.escape(id)}"]`);
    const textoActual = card ? card.querySelector(".comment-body")?.textContent || "" : "";
    const nuevoContenido = prompt("Editar opinión (PUT en MongoDB):", textoActual);
    if (nuevoContenido === null || !nuevoContenido.trim()) return;
    const nuevaNota = Number(prompt("Calificación (1 a 5 estrellas):", String(notaActual || 5))) || notaActual;
    try {
        const res = await fetch(`${API_BASE_URL}/api/comentarios/${encodeURIComponent(id)}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ contenido: nuevoContenido.trim(), calificacion: nuevaNota })
        });
        if (res.ok) cargarComentarios();
    } catch {
        alert("No se pudo actualizar la opinión.");
    }
};

window.eliminarComentarioUI = async function (id) {
    if (!confirm("¿Eliminar esta opinión de MongoDB Atlas?")) return;
    try {
        const res = await fetch(`${API_BASE_URL}/api/comentarios/${encodeURIComponent(id)}`, { method: "DELETE" });
        if (res.ok) {
            cargarComentarios();
            cargarEstadisticas();
        }
    } catch {
        alert("No se pudo eliminar la opinión.");
    }
};

async function ejecutarClasificacionLote() {
    el.dbOpsResultado.textContent = "Ejecutando updateMany en ofertas_juegos...";
    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas/clasificar-lote`, { method: "POST" });
        const data = await res.json();
        const detalle = Object.entries(data.porNivel || {})
            .map(([k, v]) => `<span class="bi-chip"><strong>${escaparHtml(k)}:</strong> ${v}</span>`)
            .join("");
        el.dbOpsResultado.innerHTML = `
            <div class="bi-summary-line">
                <strong>updateMany completado:</strong> ${data.totalModificadas || 0} ofertas clasificadas en <code>ofertas_juegos</code>.
            </div>
            <div class="bi-chips-row">${detalle}</div>
        `;
    } catch {
        el.dbOpsResultado.textContent = "No se pudo ejecutar la clasificación masiva.";
    }
}

async function ejecutarPurgaOfertas() {
    el.dbOpsResultado.textContent = "Depurando ofertas con ahorro menor al 25%...";
    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas/purgar?ahorroMenorA=25`, { method: "DELETE" });
        const data = await res.json();
        el.dbOpsResultado.innerHTML = `
            <div class="bi-summary-line">
                <strong>DELETE completado en <code>ofertas_juegos</code>:</strong>
                Eliminadas <strong>${data.eliminadas ?? 0}</strong> ofertas de bajo descuento ·
                Restantes: <strong>${data.restantes ?? 0}</strong>.
            </div>
        `;
        if (data.estadisticas) pintarEstadisticas(data.estadisticas);
        cargarOfertas();
    } catch {
        el.dbOpsResultado.textContent = "No se pudo depurar la colección de ofertas.";
    }
}

async function ejecutarReporteBi() {
    el.dbOpsResultado.textContent = "Generando agregación y guardando en colección reportes_bi...";
    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas/reporte-bi`, { method: "POST" });
        const rep = await res.json();
        const niveles = Object.entries(rep.distribucionNiveles || {});
        const totalOf = Math.max(1, Number(rep.totalOfertas || 1));
        const barrasNiveles = niveles.map(([k, v]) => {
            const pct = Math.round((Number(v) / totalOf) * 100);
            return `
                <div class="bi-bar-row">
                    <span class="bi-bar-label">${escaparHtml(k)} (${v})</span>
                    <div class="bi-bar-track"><div class="bi-bar-fill" style="--bar-pct: ${pct}%"></div></div>
                    <span class="bi-bar-pct">${pct}%</span>
                </div>
            `;
        }).join("");

        const ratings = Object.entries(rep.distribucionCalificaciones || {});
        const chipsRatings = ratings.length
            ? ratings.map(([k, v]) => `<span class="bi-chip"><strong>${escaparHtml(k)}:</strong> ${v}</span>`).join("")
            : `<span class="bi-chip">Sin calificaciones aún</span>`;

        el.dbOpsResultado.innerHTML = `
            <div class="bi-summary-line">
                Guardado en colección <code>${escaparHtml(rep.coleccionGuardada || "reportes_bi")}</code>
                (ID: <code>${escaparHtml(rep._id)}</code>) · Destacadas: <strong>${rep.ofertasDestacadas ?? 0}</strong> ·
                Alertas activas: <strong>${rep.alertasPrecioActivas ?? 0}</strong>
            </div>
            <div class="bi-bars-grid">${barrasNiveles}</div>
            <div class="bi-chips-row"><span>Calificaciones en comunidad:</span> ${chipsRatings}</div>
        `;
    } catch {
        el.dbOpsResultado.textContent = "No se pudo generar el reporte analítico.";
    }
}

iniciar();
