import {Utils} from "../util/utils"

export class QueryResults {
    headings : string[];
    dataTypes : string[];
    rows : string[][];
    rowHeadings : string[];

    getIndexOfColumn(column: string) {
        if(Utils.checkNotNullEmpty(this.headings)) {
            for(var i=0;i<this.headings.length;i++) {
                if(column == this.headings[i]) {
                    return i;
                }
            }
        }
        return -1;
    }
}