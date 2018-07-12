export class QueryText {
    query_id:string;
    user_id:string;
    query:string;
    columns:string;
    description:string;
    datastore_id:string;


    constructor(query?:string, datastore_id?:string) {
        this.query_id = '';
        this.user_id = 'logistimo';
        this.query = query;
        this.columns = '';
        this.description = '';
        this.datastore_id = datastore_id;
    }

    clone() {
        var q = new QueryText(this.query, this.datastore_id);
        q.query_id = this.query_id;
        q.user_id = this.user_id;
        q.query = this.query;
        q.columns = this.columns;
        q.description = this.description;
        q.datastore_id = this.datastore_id;
        return q;
    }

}