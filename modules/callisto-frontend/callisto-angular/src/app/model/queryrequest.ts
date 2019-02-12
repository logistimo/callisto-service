import {QueryText} from './querytext'

export class QueryRequest {
    userId : string;
    queryId : string;
    query : QueryText;
    filters;
    derivedResultsId : string;
    columnText;
    size : number;
    rowHeadings : string[];
    offset : number;
}