<%@ Page Language="C#" AutoEventWireup="true" CodeFile="example_fire_recover_batch_sign.aspx.cs" Inherits="example_fire_recover_batch_sign" %>

<%@ Register tagprefix="cf" Namespace="FIRe" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Recuperar firma de un lote firmado</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <br /><b>Firma del documento 1:</b><br /> <asp:Label runat="server" id="DocumentSignature1"></asp:Label>
        <br /><b>Firma del documento 2:</b><br /> <asp:Label runat="server" id="DocumentSignature2"></asp:Label>
    </div>
    </form>
</body>
</html>
