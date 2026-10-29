import {useState} from "react";
import PropTypes from "prop-types";
import {Box, TextField, ToggleButton, ToggleButtonGroup, Typography} from "@mui/material";

const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

const boxStyle = {display: "flex", flexWrap: "wrap", alignItems: "flex-end", gap: "8px 12px"};
const formStyle = {display: "flex", flexDirection: "column", alignItems: "center"};
const toggleButtonStyle = {padding: "3px 12px", textTransform: "none"};
const amountStyle = {width: 120};
const monthStyle = {width: 64};

const FIXED = "fixed";
const MONTHLY = "monthly";

/** Which of its two forms a planning is in: none counts as a fixed one, of nothing yet. */
const modeOf = (planning) => (planning.includes("|") ? MONTHLY : FIXED);

const monthsOf = (planning) => {
    if (planning.includes("|")) return planning.split("|");
    const amount = planning.startsWith("all=") ? planning.slice(4) : "";
    return MONTHS.map(() => amount);
};

/**
 * A row's planning, in either of the two forms the budget reads, chosen outright rather than
 * typed: fixed, the same amount every month (all=amount), or monthly, an amount for each month
 * (twelve amounts separated by |). Each form keeps what it was left at while the other is shown,
 * so switching back and forth loses nothing; a form not given yet starts from the other - the
 * fixed amount in every month, or the first month's as the fixed one.
 */
const PlanningInput = ({value, onChange, error = false}) => {
    const mode = modeOf(value);
    const months = monthsOf(value);
    // what the form not shown was last left at, so switching back finds it as it was
    const [kept, setKept] = useState(() => (mode === MONTHLY ? {monthly: months} : {fixed: months[0]}));

    const switchTo = (next) => {
        if (next === null || next === mode) return;
        if (next === MONTHLY) {
            setKept((current) => ({...current, fixed: months[0]}));
            onChange((kept.monthly ?? MONTHS.map(() => months[0])).join("|"));
        } else {
            setKept((current) => ({...current, monthly: months}));
            const amount = kept.fixed ?? months[0];
            onChange(amount === "" ? "" : "all=" + amount);
        }
    };
    const setAmount = (amount) => onChange(amount === "" ? "" : "all=" + amount);
    const setMonth = (index, amount) => onChange(months.map((month, i) => (i === index ? amount : month)).join("|"));

    return (
        <Box style={boxStyle}>
            {/* named right over the buttons that choose its form */}
            <Box style={formStyle}>
                <Typography variant="caption" color="text.secondary">Planning</Typography>
                <ToggleButtonGroup size="small" color="primary" exclusive value={mode}
                                   onChange={(event, next) => switchTo(next)} aria-label="Planning form">
                    <ToggleButton value={FIXED} style={toggleButtonStyle}>Fixed</ToggleButton>
                    <ToggleButton value={MONTHLY} style={toggleButtonStyle}>Monthly</ToggleButton>
                </ToggleButtonGroup>
            </Box>
            {mode === FIXED
                ? <TextField variant="standard" size="small" type="number" label="all=" style={amountStyle}
                             inputProps={{"aria-label": "Planning"}}
                             value={months[0]} error={error} onChange={(event) => setAmount(event.target.value)}/>
                : MONTHS.map((month, index) => (
                    <TextField key={month} variant="standard" size="small" type="number" label={month} style={monthStyle}
                               value={months[index]} error={error && months[index].trim() === ""}
                               onChange={(event) => setMonth(index, event.target.value)}/>
                ))
            }
        </Box>
    );
};

PlanningInput.propTypes = {
    /** As stored: all=amount, twelve amounts separated by |, or empty for none. */
    value: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    /** Whether it is marked as missing or wrong. */
    error: PropTypes.bool,
};

export default PlanningInput;
