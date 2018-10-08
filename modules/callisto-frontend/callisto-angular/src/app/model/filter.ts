export class Filter {

    id : string;
    filter_id: string;
    placeholder: string;
    name: string;
    auto_complete_config : {
        placeholder: string,
        value_column_name: string;
        display_column_name: string;
    };
    rename_query_id: string;
    is_column_filter: boolean;
}