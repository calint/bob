let zen_run_interval_id = 0;
let zen_tx_buf = [];
const fps = 1000 / 60;
function zen_run_start() {
    if (zen_run_interval_id != 0)
        return;
    zen_run_interval_id = setInterval(zen_tick, fps);
}

function zen_tick() {
    if (ui.is_busy)
        return;
    $x('- t ' + (zen_tx_buf.length > 0 ? zen_tx_buf.shift() : 0));
}

function zen_run_stop() {
    if (zen_run_interval_id == 0)
        return;
    clearInterval(zen_run_interval_id);
    zen_run_interval_id = 0;
}

function zen_reset() {
    zen_run_stop();
    $x('- rst');
}
