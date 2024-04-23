import React from "react";

const Url = ({url,description}) => {
    return ( <div>
        <a href={url}>{url}</a>
        <p>{description}</p>
    </div>);
}

export default Url;