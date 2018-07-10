import {QueryResults} from "../model/queryresult";
import {Filter} from "../model/filter";
import {FilterResult} from "../model/filter-result";
import {Utils} from "../util/utils";


export class FilterResultsAdapterUtils {

    public static getFilterResultsFromQueryResults(queryResults:QueryResults, filter:Filter) : FilterResult[] {
        const filterResults: FilterResult[] = new Array();
        if(Utils.checkNotNullEmpty(queryResults) && Utils.checkNotNullEmpty(queryResults.rows)) {
            const filterDisplayColumnIndex = this.getIndexOfColumn(filter.filter_display_column_name, queryResults);
            const filterValueColumnIndex = this.getIndexOfColumn(filter.filter_value_column_name, queryResults);
            for(var i=0;i<queryResults.rows.length;i++) {
                const filterResult = new FilterResult();
                filterResult.name = queryResults.rows[i][filterDisplayColumnIndex];
                filterResult.value = queryResults.rows[i][filterValueColumnIndex];
                filterResults.push(filterResult);
            }
        }
        return filterResults;
    }

    private static getIndexOfColumn(column: string, queryResults: QueryResults) {
        if(Utils.checkNotNullEmpty(queryResults.headings)) {
            for(var i=0;i<queryResults.headings.length;i++) {
                if(column == queryResults.headings[i]) {
                    return i;
                }
            }
        }
        return -1;
    }
}