import PropTypes from "prop-types";
import {Bar, BarChart, Brush, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis} from "recharts";
import {useData} from "../../fetch";
import DataView from "../common/DataView";

/** The bar chart for one accounting dataset. */
const AccountingBarChart = ({config}) => {
    const {data, loaded, error} = useData("/chart/data/" + config.id);

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <div style={{height: "85vh", width: "90vw"}}>
                    <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={data.values} margin={{top: 5, right: 30, left: 30, bottom: 5}}>
                            <CartesianGrid strokeDasharray="3 3"/>
                            <XAxis dataKey="label"/>
                            <YAxis/>
                            <Tooltip/>
                            <Bar dataKey={config.type.toLowerCase()} fill="#8884d8"/>
                            <Brush/>
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            )}
        </DataView>
    );
};

AccountingBarChart.propTypes = {
    config: PropTypes.object.isRequired,
};

export default AccountingBarChart;
