export class QueryText {
    query_id:string;
    user_id:string;
    query:string;
    columns:string;
    server_id:string;

    constructor() {
        this.query_id = '';
        this.user_id = 'logistimo';
        this.query = '';
        this.columns = '';
        this.server_id = '';
    }

    clone() {
        var q = new QueryText();
        q.query_id = this.query_id;
        q.user_id = this.user_id;
        q.query = this.query;
        q.columns = this.columns;
        q.server_id = this.server_id;
        return q;
    }

}