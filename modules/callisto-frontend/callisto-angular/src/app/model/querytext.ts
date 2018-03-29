export class QueryText {
    query_id:string;
    user_id:string;
    query:string;
    columns:string;
    server_id:string;

    constructor(query:string, server_id:string) {
        this.query_id = '';
        this.user_id = 'logistimo';
        this.query = query;
        this.columns = '';
        this.server_id = server_id;
    }

    clone() {
        var q = new QueryText(this.query, this.server_id);
        q.query_id = this.query_id;
        q.user_id = this.user_id;
        q.query = this.query;
        q.columns = this.columns;
        q.server_id = this.server_id;
        return q;
    }

}