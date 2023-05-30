let zen_run_do = false;
let zen_run_interval_id = 0;
function zen_run_toggle() {
    zen_run_do = !zen_run_do;
    if (zen_run_do) {
        zen_run_interval_id = setInterval(zen_run, 100);
    } else {
        clearInterval(zen_run_interval_id);
    }
}

function zen_run() {
    if (ui.is_busy)
        return;
    $x('- t');
}
